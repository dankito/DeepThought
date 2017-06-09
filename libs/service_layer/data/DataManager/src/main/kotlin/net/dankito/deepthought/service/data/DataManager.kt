package net.dankito.deepthought.service.data

import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.DeepThoughtApplication
import net.dankito.deepthought.model.User
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


class DataManager(val entityManager: IEntityManager, private val configuration: EntityManagerConfiguration, private val defaultDataInitializer: DefaultDataInitializer) {

    companion object {
        private val log = LoggerFactory.getLogger(DataManager::class.java)
    }


    private lateinit var application: DeepThoughtApplication
    private lateinit var loggedOnUser: User
    private var currentDeepThought: DeepThought? = null

    var isInitialized = false
        private set

    private val initializationListeners = mutableSetOf<() -> Unit>()


    init {
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

    private fun retrieveBasicData(): DeepThought? {
        try {
            val applicationsQueryResult = entityManager.getAllEntitiesOfType(DeepThoughtApplication::class.java)

            if (applicationsQueryResult.size > 0) { // TODO: what to do if there's more than one DeepThoughtApplication instance persisted?
                application = applicationsQueryResult[0]
                loggedOnUser = application.lastLoggedOnUser
                // TODO: set application language according to user's settings

                // TODO: what to return if user was already logged on but autoLogOn is set to false?
                if (application.autoLogOnLastLoggedOnUser) {
                    currentDeepThought = loggedOnUser.lastViewedDeepThought

                    return currentDeepThought
                }
            }
        } catch (ex: Exception) {
            log.error("Could not deserialize DeepThoughtApplication", ex)
            // TODO: determine if this is ok because this is the first Application start or if a severe error occurred?
        }


        return createAndPersistDefaultDeepThought()
    }

    protected fun createAndPersistDefaultDeepThought(): DeepThought? {
        application = defaultDataInitializer.createDefaultData()
        loggedOnUser = application.lastLoggedOnUser

        currentDeepThought = loggedOnUser.lastViewedDeepThought

        entityManager.persistEntity(application)

        return currentDeepThought
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