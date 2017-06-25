package net.dankito.deepthought.javafx.appstart

import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.service.search.ISearchEngine
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


/**
 * Local device isn't available right at start, we have to wait at least till DataManager is initialized.
 *
 * To provide a more smooth application start up, we wait till all base data is retrieved.
 * Therefore after SearchEngine is initialized we give application some time to show initial data
 * before we start communicator classes like @see DevicesDiscoverer, @see IClientCommunicator, @see ISyncManager, ...
 */
class CommunicationManagerStarter(searchEngine: ISearchEngine) {

    companion object {
        const val DefaultWaitTimeBeforeStartingCommunicationManagerMillis = 5000L
    }


    @Inject
    protected lateinit var communicationManager: ICommunicationManager


    init {
        searchEngine.addInitializationListener { searchEngineInitialized() }
    }

    private fun searchEngineInitialized() {
        val timer = Timer()

        timer.schedule(DefaultWaitTimeBeforeStartingCommunicationManagerMillis) {
            startCommunicationManager()
        }
    }


    private fun startCommunicationManager() {
        AppComponent.component.inject(this) // and only now create CommunicationManager as now localDevice is loaded from Db

        communicationManager.startAsync()
    }

}