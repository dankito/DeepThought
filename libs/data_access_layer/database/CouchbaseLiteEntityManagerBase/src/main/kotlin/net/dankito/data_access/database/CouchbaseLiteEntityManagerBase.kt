package net.dankito.data_access.database

import com.couchbase.lite.*
import net.dankito.deepthought.model.BaseEntity
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.JPAEntityConfiguration
import net.dankito.jpa.apt.generated.GeneratedEntityConfigs
import net.dankito.jpa.cache.DaoCache
import net.dankito.jpa.cache.ObjectCache
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.jpa.util.DatabaseCompacter
import net.dankito.util.AsyncProducerConsumerQueue
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.version.Versions
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


abstract class CouchbaseLiteEntityManagerBase(protected var context: Context, private val localSettingsStore: ILocalSettingsStore) : IEntityManager {

    companion object {
        private val log = LoggerFactory.getLogger(CouchbaseLiteEntityManagerBase::class.java)
    }


    lateinit var manager: Manager
        private set

    lateinit var database: Database
        private set

    private lateinit var databaseCompacter: DatabaseCompacter

    protected var daoCache = DaoCache()

    var objectCache = ObjectCache()
        private set

    private lateinit var _databasePath: String

    protected var mapEntityClassesToDaos: MutableMap<Class<*>, Dao> = HashMap()


    override fun open(configuration: EntityManagerConfiguration) {
        this._databasePath = adjustDatabasePath(context, configuration)

        createDatabase(configuration)

        databaseCompacter = DatabaseCompacter(database, 10000)

        val result = loadGeneratedModel()

        createDaos(result)

        checkDataModelVersion()
    }

    private fun loadGeneratedModel(): JPAEntityConfiguration {
        val generatedConfigs = GeneratedEntityConfigs()

        val generatedEntityConfigs = generatedConfigs.getGeneratedEntityConfigs()

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
        return Dao(database, entityConfig, objectCache, daoCache, databaseCompacter)
    }


    private fun checkDataModelVersion() {
        val databaseDataModelVersion = localSettingsStore.getDatabaseDataModelVersion()
        val appDataModelVersion = Versions.DataModelVersion

        if(appDataModelVersion != databaseDataModelVersion) {
            adjustEntitiesToDataModelVersion(databaseDataModelVersion, appDataModelVersion)

            localSettingsStore.setDatabaseDataModelVersion(appDataModelVersion)
        }
    }

    private fun adjustEntitiesToDataModelVersion(databaseDataModelVersion: Int, appDataModelVersion: Int) {
        // implement as soon as we have first data model incompatibilities
    }


    override fun optimizeDatabase() {
        databaseCompacter.scheduleCompacting()
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

    override fun updateEntities(entities: List<Any>): Boolean {
        var result = true

        for(entity in entities) {
            result = result and updateEntity(entity)
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
            log.error("It was requested to persist or update Entity of type $entityClass, but mapEntityClassesToDaos does not contain an Item for this Entity.")
        }

        return dao
    }


    /**
     * Passes all entities that have been updated after (Couchbase database) sequenceNumber on to queue.
     * Using a AsyncProducerConsumerQueue so that retrieved entities can be consumed immediately and not loading all of them into memory first
     * which could result in an OutOfMemory exception when there are too many changes for little Android's memory.
     */
    override fun <T> getAllEntitiesUpdatedAfter(sequenceNumber: Long, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>): Long {
        val currentSequenceNumber = database.lastSequenceNumber

        val options = ChangesOptions(Int.MAX_VALUE, true, true, true)
        val changes = database.changesSince(sequenceNumber, options, null, null)
        log.info("Retrieved ${changes.allDocIds.size} changes since $sequenceNumber")

        val anyDao = mapEntityClassesToDaos.values.first()
        changes.allDocIds.forEach { docId ->
            getUpdatedEntityFromDocument(docId, anyDao, queue)
        }

        return currentSequenceNumber
    }

    private fun <T> getUpdatedEntityFromDocument(documentId: String, anyDao: Dao, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        try {
            database.getDocument(documentId)?.let {
                getUpdatedEntityFromDocument(it, anyDao, queue)
            }
        } catch(e: Exception) {
            log.error("Could not get updated entity for document id $documentId", e)
        }
    }

    private fun <T> getUpdatedEntityFromDocument(document: Document, anyDao: Dao, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        if(document.isDeleted || document.getProperty(Dao.DELETED_SYSTEM_COLUMN_NAME) == true) {
            getDeletedEntityFromDocument(document, queue)
        }
        else {
            anyDao.getEntityClassFromDocument(document)?.let { entityClass ->
                getObjectForDocument(entityClass as Class<T>, document, queue)
            }
        }
    }

    private fun <T> getObjectForDocument(entityClass: Class<T>, document: Document, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        (objectCache.get(entityClass, document.id) as? T)?.let { // first check if an object for that id is already cached
            queue.add(ChangedEntity<T>(entityClass, it, (it as BaseEntity).id))
            return
        }

        getDaoForClass(entityClass)?.let { dao ->
            (dao.createObjectFromDocument(document, document.id, entityClass) as? T)?.let { entity ->
                queue.add(ChangedEntity<T>(entityClass, entity, (entity as BaseEntity).id))
            }
        }
    }

    private fun <T> getDeletedEntityFromDocument(document: Document, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        findLastUndeletedRevision(document)?.let { lastUndeletedRevision ->
            getEntityTypeFromRevision(lastUndeletedRevision)?.let { entityType ->
                try {
                    queue.add(ChangedEntity(entityType as Class<T>, null, document.id, true))
                } catch(e: Exception) { log.error("Could not get deleted entity for entity $entityType with id ${document.id}", e) }
            }
        }
    }


    private fun loadDeletedEntity(documentId: String): BaseEntity? {
        val document = database.getDocument(documentId)
        if (document != null) {
            val lastUndeletedRevision = findLastUndeletedRevision(document)

            if (lastUndeletedRevision != null) {
                val entityType = getEntityTypeFromRevision(lastUndeletedRevision)
                if (entityType != null) {
                    return getDeletedEntity(documentId, entityType, lastUndeletedRevision.document)
                }
            }
        }

        return null
    }

    private fun getDeletedEntity(id: String, entityType: Class<out BaseEntity>, document: Document): BaseEntity? {
//        getEntityById(entityType, id)?.let {
//            return it
//        }

        val dao = getDaoForClass(entityType)
        dao?.createObjectFromDocument(document, id, entityType)?.let {
            return it as? BaseEntity
        }

        return null
    }

    private fun findLastUndeletedRevision(document: Document): SavedRevision? {
        try {
            val leafRevisions = document.leafRevisions
            if (leafRevisions.size > 0) {
                var parentId: String? = leafRevisions[0].parentId

                while(parentId != null) {
                    val parentRevision = document.getRevision(parentId)

                    if(parentRevision == null) {
                        return null
                    }

                    if (parentRevision.isDeletion == false) {
                        return parentRevision
                    }

                    parentId = parentRevision.getParentId()
                }
            }
        } catch (e: Exception) {
            log.error("Could not get Revision History for deleted Document with id " + document.getId(), e)
        }

        return null
    }

    private fun getEntityTypeFromRevision(revision: SavedRevision): Class<out BaseEntity>? {
        val entityTypeString = revision.getProperty(Dao.TYPE_COLUMN_NAME) as String

        return getEntityTypeFromEntityTypeString(entityTypeString)
    }

    private fun getEntityTypeFromEntityTypeString(entityTypeString: String?): Class<out BaseEntity>? {
        if (entityTypeString != null) { // sometimes there are documents without type or any other column/property except Couchbase's system properties (like _id)
            try {
                return Class.forName(entityTypeString) as? Class<out BaseEntity>
            } catch (e: Exception) {
                log.error("Could not get class for entity type " + entityTypeString)
            }
        }

        return null
    }

}
