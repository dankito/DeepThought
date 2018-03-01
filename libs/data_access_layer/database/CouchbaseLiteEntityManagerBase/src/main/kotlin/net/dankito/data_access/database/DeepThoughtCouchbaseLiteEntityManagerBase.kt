package net.dankito.data_access.database

import com.couchbase.lite.*
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.JPAEntityConfiguration
import net.dankito.jpa.apt.generated.GeneratedEntityConfigs
import net.dankito.jpa.cache.DaoCache
import net.dankito.jpa.cache.ObjectCache
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.jpa.util.DatabaseCompacter
import net.dankito.synchronization.database.EntityManagerConfiguration
import net.dankito.synchronization.database.IEntityManager
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.utils.version.Versions
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


abstract class DeepThoughtCouchbaseLiteEntityManagerBase(protected var context: Context, private val localSettingsStore: ILocalSettingsStore) : IEntityManager {

    companion object {
        private val log = LoggerFactory.getLogger(DeepThoughtCouchbaseLiteEntityManagerBase::class.java)
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

}
