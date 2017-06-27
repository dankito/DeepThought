package net.dankito.deepthought.service.data

import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.DeepThoughtApplication
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.User
import net.dankito.utils.IPlatformConfiguration
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.concurrent.thread


class DataManager(val entityManager: IEntityManager, private val configuration: EntityManagerConfiguration,
                  private val defaultDataInitializer: DefaultDataInitializer, private val platformConfiguration: IPlatformConfiguration) {

    companion object {
        private val log = LoggerFactory.getLogger(DataManager::class.java)
    }


    lateinit var application: DeepThoughtApplication

    lateinit var loggedOnUser: User
    lateinit var localDevice: Device

    var dataFolderPath: File

    var isInitialized = false
        private set

    private val initializationListeners = mutableSetOf<() -> Unit>()


    init {
        dataFolderPath = platformConfiguration.getDefaultDataFolder()

        thread {
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
            val applicationsQueryResult = entityManager.getAllEntitiesOfType(DeepThoughtApplication::class.java)

            if (applicationsQueryResult.size > 0) { // TODO: what to do if there's more than one DeepThoughtApplication instance persisted?
                application = applicationsQueryResult[0]

                loggedOnUser = application.lastLoggedOnUser
                localDevice = application.localDevice

                // TODO: set application language according to user's settings
            }
        } catch (ex: Exception) {
            log.error("Could not deserialize DeepThoughtApplication", ex)
            // TODO: determine if this is ok because this is the first Application start or if a severe error occurred?
        }


        createAndPersistDefaultDeepThought()
    }

    protected fun createAndPersistDefaultDeepThought() {
        application = defaultDataInitializer.createDefaultData()

        loggedOnUser = application.lastLoggedOnUser
        localDevice = application.localDevice

        entityManager.persistEntity(application)
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