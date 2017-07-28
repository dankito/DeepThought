package net.dankito.deepthought.android.service.network

import android.content.Intent
import net.dankito.utils.services.network.NetworkConnectivityManagerBase
import net.dankito.utils.services.network.NetworkHelper



class AndroidNetworkConnectivityManager(networkHelper: NetworkHelper) : NetworkConnectivityManagerBase(networkHelper) {


    fun handleConnectivityChangedIntent(intent: Intent) {
        networkInterfacesChanged()
    }

}