package net.dankito.deepthought.service.data

import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.LocalSettings
import net.dankito.deepthought.model.User
import net.dankito.utils.IPlatformConfiguration
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
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
                val settings = deepThought.localSettings
                if(settings != null) {
                    this.localSettings = settings
                }
                else { // TODO: remove after next alpha release
                    this.localSettings = LocalSettings(1, 1, 1, Date(0), Date(0), false, false, false)
                    deepThought.localSettings = localSettings
                    entityManager.persistEntity(localSettings)
                    entityManager.updateEntity(deepThought)
                }

                return
            }
        } catch (ex: Exception) {
            log.error("Could not deserialize DeepThought", ex)
            // TODO: determine if this is ok because this is the first app start or if a severe error occurred?
        }


        createAndPersistDefaultDeepThought()
    }

    protected fun createAndPersistDefaultDeepThought() {
        deepThought = defaultDataInitializer.createDefaultData()

        localUser = deepThought.localUser
        localDevice = deepThought.localDevice
        deepThought.localSettings?.let { localSettings = it } // TODO: change after next alpha release

        entityManager.persistEntity(deepThought)
    }


    fun localSettingsUpdated() {
        entityManager.updateEntity(localSettings)
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
}