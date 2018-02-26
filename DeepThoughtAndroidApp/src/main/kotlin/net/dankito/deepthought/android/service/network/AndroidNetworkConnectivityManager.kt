package net.dankito.deepthought.android.service.network

import android.content.Intent
import net.dankito.util.network.NetworkConnectivityManagerBase
import net.dankito.util.network.NetworkHelper



class AndroidNetworkConnectivityManager(networkHelper: NetworkHelper) : NetworkConnectivityManagerBase(networkHelper) {


    fun handleConnectivityChangedIntent(intent: Intent) {
        networkInterfacesChanged()
    }

}