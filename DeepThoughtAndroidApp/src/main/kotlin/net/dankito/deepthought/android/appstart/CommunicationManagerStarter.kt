package net.dankito.deepthought.android.appstart

import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.service.communication.CommunicationManagerStarterBase
import net.dankito.deepthought.service.data.DataManager


/**
 * Local device isn't available right at start, we have to wait at least till DataManager is initialized.
 *
 * To provide a more smooth application start up, we wait till all base data is retrieved.
 * Therefore after SearchEngine is initialized we give application some time to show initial data
 * before we start communicator classes like @see DevicesDiscoverer, @see IClientCommunicator, @see ISyncManager, ...
 */
class CommunicationManagerStarter(dataManager: DataManager) : CommunicationManagerStarterBase(dataManager) {

    override fun injectDependencies() {
        AppComponent.component.inject(this)
    }

}