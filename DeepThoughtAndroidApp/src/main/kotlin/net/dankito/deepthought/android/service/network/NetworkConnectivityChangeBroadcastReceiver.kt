package net.dankito.deepthought.android.service.network

import net.dankito.deepthought.android.di.AppComponent
import net.dankito.utils.android.network.AndroidNetworkConnectivityManager
import net.dankito.utils.android.network.NetworkConnectivityChangeBroadcastReceiverBase
import net.dankito.utils.network.INetworkConnectivityManager
import javax.inject.Inject


class NetworkConnectivityChangeBroadcastReceiver : NetworkConnectivityChangeBroadcastReceiverBase() {

    @Inject
    protected lateinit var networkConnectivityManager: INetworkConnectivityManager

    init {
        AppComponent.component.inject(this)
    }


    override fun getNetworkConnectivityManager(): net.dankito.utils.android.network.AndroidNetworkConnectivityManager {
        return networkConnectivityManager as AndroidNetworkConnectivityManager
    }

}