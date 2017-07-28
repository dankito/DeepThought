package net.dankito.deepthought.android.service.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.utils.services.network.INetworkConnectivityManager
import javax.inject.Inject


class NetworkConnectivityChangeBroadcastReceiver : BroadcastReceiver() {

    @Inject
    protected lateinit var networkConnectivityManager: INetworkConnectivityManager

    init {
        AppComponent.component.inject(this)
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            (networkConnectivityManager as? AndroidNetworkConnectivityManager)?.handleConnectivityChangedIntent(intent)
        }
    }

}