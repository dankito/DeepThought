package net.dankito.deepthought.javafx.appstart

import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.synchronization.FileServer
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.service.data.DataManager
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


/**
 * Local device isn't available right at start, we have to wait at least till DataManager is initialized.
 *
 * To provide a more smooth application start up, we wait till all base data is retrieved.
 * Therefore after DataManager is initialized we give application some time to show initial data
 * before we start communicator classes like @see DevicesDiscoverer, @see IClientCommunicator, @see ISyncManager, ...
 */
class CommunicationManagerStarter(dataManager: DataManager) {

    companion object {
        const val DefaultWaitTimeBeforeStartingCommunicationManagerMillis = 5000L
    }


    @Inject
    protected lateinit var fileServer: FileServer

    @Inject
    protected lateinit var fileManager: FileManager // to create FileManager so that listening for changes to FileLinks and FileSyncService is started

    @Inject
    protected lateinit var communicationManager: ICommunicationManager


    init {
        dataManager.addInitializationListener { dataManagerInitialized() }
    }

    private fun dataManagerInitialized() {
        Timer().schedule(DefaultWaitTimeBeforeStartingCommunicationManagerMillis) {
            startCommunicationManager()
        }
    }


    private fun startCommunicationManager() {
        AppComponent.component.inject(this) // and only now create CommunicationManager as now localDevice is loaded from Db

        fileServer.startServerAsync { // start FileServer first so that fileSynchronizationPort get set before communicationManager sends it to synchronized devices
            communicationManager.startAsync()
        }
    }

}