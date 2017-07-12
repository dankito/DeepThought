package net.dankito.data_access.database

import com.couchbase.lite.*
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.JPAEntityConfiguration
import net.dankito.jpa.cache.DaoCache
import net.dankito.jpa.cache.ObjectCache
import net.dankito.jpa.couchbaselite.Dao
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


abstract class CouchbaseLiteEntityManagerBase(protected var context: Context) : IEntityManager {

    companion object {
        private val log = LoggerFactory.getLogger(CouchbaseLiteEntityManagerBase::class.java)
    }


    lateinit var manager: Manager
        private set

    lateinit var database: Database
        private set

    protected var daoCache = DaoCache()

    var objectCache = ObjectCache()
        private set

    private lateinit var _databasePath: String

    protected var mapEntityClassesToDaos: MutableMap<Class<*>, Dao> = HashMap()


    override fun open(configuration: EntityManagerConfiguration) {
        this._databasePath = adjustDatabasePath(context, configuration)

        createDatabase(configuration)

        val result = loadGeneratedModel()

        createDaos(result)
    }

    private fun loadGeneratedModel(): JPAEntityConfiguration {
        val generatedConfigsClass = Class.forName("net.dankito.jpa.apt.generated.GeneratedEntityConfigs")
        val generatedConfigs = generatedConfigsClass.newInstance()

        val getGeneratedEntityConfigsMethod = generatedConfigsClass.getDeclaredMethod("getGeneratedEntityConfigs")

        val generatedEntityConfigs = getGeneratedEntityConfigsMethod.invoke(generatedConfigs) as List<EntityConfig>

        return JPAEntityConfiguration(generatedEntityConfigs)
    }

    override fun close() {
        manager.close()
    }

    protected abstract fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String


    @Throws(CouchbaseLiteException::class, IOException::class)
    protected fun createDatabase(configuration: EntityManagerConfiguration) {
        val managerOptions = Manager.DEFAULT_OPTIONS
        manager = Manager(context, managerOptions)

        val options = DatabaseOptions()
        options.isCreate = true

        database = manager.openDatabase(configuration.databaseName, options)
    }

    protected fun createDaos(result: JPAEntityConfiguration) {
        for (entityConfig in result.entities) {
            val entityDao = createDaoForEntity(entityConfig)

            daoCache.addDao(entityConfig.entityClass, entityDao)
            mapEntityClassesToDaos.put(entityConfig.entityClass, entityDao)
        }
    }

    protected fun createDaoForEntity(entityConfig: EntityConfig): Dao {
        return Dao(database, entityConfig, objectCache, daoCache)
    }


    override fun getDatabasePath(): String {
        return _databasePath
    }

    override fun persistEntity(entity: Any): Boolean {
        try {
            val dao = getDaoForEntity(entity)

            if (dao != null) {
                return dao.create(entity)
            }
        } catch (e: Exception) {
            log.error("Could not create entity " + entity, e)
        }

        return false
    }

    override fun updateEntity(entity: Any): Boolean {
        try {
            val dao = getDaoForEntity(entity)

            if (dao != null) {
                return dao.update(entity)
            }
        } catch (e: Exception) {
            log.error("Could not update entity " + entity, e)
        }

        return false
    }

    override fun updateEntities(entities: List<*>): Boolean {
        var result = true

        for (entity in entities) {
            result = result and updateEntity(entity as Any)
        }

        return result
    }

    override fun deleteEntity(entity: Any): Boolean {
        try {
            val dao = getDaoForEntity(entity)

            if (dao != null) {
                return dao.delete(entity)
            }
        } catch (e: Exception) {
            log.error("Could not delete entity " + entity, e)
        }

        return false
    }

    override fun <T> getEntityById(type: Class<T>, id: String): T? {
        try {
            val dao = getDaoForClass(type)

            if (dao != null) {
                return dao.retrieve(id) as T
            }
        } catch (e: Exception) {
            log.error("Could not get entity of type $type for id $id", e)
        }

        return null
    }

    override fun <T> getEntitiesById(type: Class<T>, ids: Collection<String>, keepOrderingOfIds: Boolean): List<T> {
        val resultEntities = ArrayList<T>()

        for (id in ids) {
            resultEntities.add(getEntityById(type, id) as T)
        }

        return resultEntities
    }

    override fun <T> getAllEntitiesOfType(type: Class<T>): List<T> {
        try {
            val dao = getDaoForClass(type)

            if (dao != null) {
                return dao.retrieveAllEntitiesOfType(type)
            }
        } catch (e: Exception) {
            log.error("Could not get all entities of type " + type, e)
        }

        return ArrayList()
    }


    protected fun getDaoForEntity(entity: Any?): Dao? {
        if (entity == null) {
            log.error("Caught that a Database operation was tried to perform on a null Entity Object")
            return null
        }

        return getDaoForClass(entity.javaClass)
    }

    fun getDaoForClass(entityClass: Class<*>): Dao? {
        val dao = mapEntityClassesToDaos[entityClass]
        if (dao == null) {
            log.error("It was requested to persist or update Entity of type $entityClass, but mapEntityClassesToDaos does not contain an Entry for this Entity.")
        }

        return dao
    }

}
