package net.dankito.deepthought.android.appstart

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.androidservice.PermanentNotificationService
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.network.NetworkConnectivityChangeBroadcastReceiver
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject


class AndroidAppInitializer {

    // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
    // speeding app start up a bit.
    // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices

    @Inject
    protected lateinit var context: Context

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var communicationManagerStarter: CommunicationManagerStarter // same here: just create instance, CommunicationManagerStarter initializes itself

    @Inject
    protected lateinit var crashReporter: ICrashReporter

    @Inject
    protected lateinit var permanentNotificationService: PermanentNotificationService


    fun initializeApp() {
        AppComponent.component.inject(this)

        initializeCrashReporter()

        initializeNetworkConnectivityChangeBroadcastReceiver()

        initializePermanentNotification()
    }

    private fun initializeCrashReporter() {
        crashReporter.init(context, dataManager)
    }


    private fun initializeNetworkConnectivityChangeBroadcastReceiver() {
        // Apps targeting Android 7.0 (API level 24) and higher do not receive this broadcast if they declare the broadcast receiver in their manifest.
        // Apps will still receive broadcasts if they register their BroadcastReceiver with Context.registerReceiver() and that context is still valid.
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) // TODO: not equal to AndroidManifest and not handled in NetworkConnectivityChangeBroadcastReceiver

        context.registerReceiver(NetworkConnectivityChangeBroadcastReceiver(), intentFilter)
    }


    private fun initializePermanentNotification() {
        permanentNotificationService.showPermanentNotification()
    }

}