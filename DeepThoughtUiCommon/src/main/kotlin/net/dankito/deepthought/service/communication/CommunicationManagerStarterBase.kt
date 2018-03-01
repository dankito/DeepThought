package net.dankito.deepthought.service.communication

import net.dankito.deepthought.files.DeepThoughtFileManager
import net.dankito.deepthought.files.synchronization.FileServer
import net.dankito.deepthought.service.data.DataManager
import net.dankito.synchronization.service.ICommunicationManager
import net.dankito.utils.services.Times
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


/**
 * Local device isn't available right at start, we have to wait at least till DataManager is initialized.
 *
 * To provide a more smooth application start up, we wait till all base data is retrieved.
 * Therefore after DataManager is initialized we give application some time to show initial data
 * before we start communicator classes like @see DevicesDiscoverer, @see IMessenger, @see ISyncManager, ...
 */
abstract class CommunicationManagerStarterBase(dataManager: DataManager) {

    @Inject
    protected lateinit var fileServer: FileServer

    @Inject
    protected lateinit var fileManager: DeepThoughtFileManager // to create FileManager so that listening for changes to FileLinks and FileSyncService is started

    @Inject
    protected lateinit var communicationManager: ICommunicationManager


    init {
        dataManager.addInitializationListener { dataManagerInitialized() }
    }

    private fun dataManagerInitialized() {
        Timer().schedule(Times.DefaultWaitTimeBeforeStartingCommunicationManagerMillis) {
            startCommunicationManager()
        }
    }


    private fun startCommunicationManager() {
        injectDependencies() // and only now create CommunicationManager as now localDevice is loaded from Db

        fileServer.startServerAsync { // start FileServer first so that fileSynchronizationPort get set before communicationManager sends it to synchronized devices
            communicationManager.startAsync()
        }
    }


    abstract protected fun injectDependencies()

}