package net.dankito.deepthought.service.data

import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.LocalSettings
import net.dankito.deepthought.model.User
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.services.Times
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class DataManager(val entityManager: IEntityManager, private val configuration: EntityManagerConfiguration,
                  private val defaultDataInitializer: DefaultDataInitializer, platformConfiguration: IPlatformConfiguration) {

    companion object {
        private val log = LoggerFactory.getLogger(DataManager::class.java)
    }


    lateinit var deepThought: DeepThought

    lateinit var localUser: User
    lateinit var localDevice: Device
    lateinit var localSettings: LocalSettings

    var dataFolderPath: File = platformConfiguration.getDefaultDataFolder()

    var isInitialized = false
        private set

    private val initializationListeners = mutableSetOf<() -> Unit>()

    private val localSettingsChangedListeners = mutableSetOf<(LocalSettings) -> Unit>()


    init {
        thread(priority = Thread.MAX_PRIORITY) {
            initializeDataManager()
        }
    }

    private fun initializeDataManager() {
        initializeEntityManager()

        retrieveBasicData()

        dataManagerInitialized()
    }

    private fun initializeEntityManager() {
        entityManager.open(configuration)
    }

    private fun retrieveBasicData() {
        try {
            val deepThoughtQueryResult = entityManager.getAllEntitiesOfType(DeepThought::class.java)

            if (deepThoughtQueryResult.isNotEmpty()) { // TODO: what to do if there's more than one DeepThought instance persisted?
                deepThought = deepThoughtQueryResult[0]

                localUser = deepThought.localUser
                localDevice = deepThought.localDevice
                localSettings = deepThought.localSettings

                mayOptimizeDatabase()

                return
            }
        } catch (ex: Exception) {
            log.error("Could not deserialize DeepThought", ex)
            // TODO: determine if this is ok because this is the first app start or if a severe error occurred?
        }


        createAndPersistDefaultDeepThought()
    }

    private fun createAndPersistDefaultDeepThought() {
        deepThought = defaultDataInitializer.createDefaultData()

        localUser = deepThought.localUser
        localDevice = deepThought.localDevice
        localSettings = deepThought.localSettings

        entityManager.persistEntity(deepThought)
    }

    private fun mayOptimizeDatabase() {
        Timer().schedule(Times.DefaultDelayBeforeOptimizingDatabaseSeconds * 1000L) {
            optimizeDatabaseIfNeeded()
        }
    }

    private fun optimizeDatabaseIfNeeded() {
        val startTime = Date()
        val timeSinceLastOptimizationMillis = startTime.time - localSettings.lastDatabaseOptimizationTime.time
        if(timeSinceLastOptimizationMillis > Times.DefaultIntervalToRunDatabaseOptimizationDays * 24 * 60 * 60 * 1000) {
            optimizeDatabase()

            localSettings.lastDatabaseOptimizationTime = startTime
            localSettingsUpdated()
        }
    }

    private fun optimizeDatabase() {
        entityManager.optimizeDatabase()
    }


    fun localSettingsUpdated() {
        entityManager.updateEntity(localSettings)

        callLocalSettingsChangedListeners(localSettings)
    }


    fun addInitializationListener(listener: () -> Unit) {
        if(isInitialized) {
            callInitializationListener(listener)
        }
        else {
            initializationListeners.add(listener)
        }
    }

    private fun dataManagerInitialized() {
        isInitialized = true

        for(listener in HashSet<() -> Unit>(initializationListeners)) {
            callInitializationListener(listener)
        }

        initializationListeners.clear()
    }

    private fun callInitializationListener(listener: () -> Unit) {
        listener()
    }


    fun addLocalSettingsChangedListener(listener: (LocalSettings) -> Unit) {
        localSettingsChangedListeners.add(listener)
    }

    fun removeLocalSettingsChangedListener(listener: (LocalSettings) -> Unit) {
        localSettingsChangedListeners.remove(listener)
    }

    private fun callLocalSettingsChangedListeners(settings: LocalSettings) {
        localSettingsChangedListeners.forEach { listener ->
            listener(settings)
        }
    }

}