package net.dankito.deepthought.android.appstart

import android.content.Context
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.android.views.html.AndroidHtmlEditorPool
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.html.HtmlEditorExtractor
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject
import android.net.wifi.WifiManager
import android.net.ConnectivityManager
import android.content.IntentFilter
import net.dankito.deepthought.android.service.network.NetworkConnectivityChangeBroadcastReceiver


class AndroidAppInitializer {

    // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
    // speeding app start up a bit.
    // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices

    @Inject
    protected lateinit var context: Context

    @Inject
    protected lateinit var activityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var htmlEditorExtractor: HtmlEditorExtractor

    @Inject
    protected lateinit var htmlEditorPool: AndroidHtmlEditorPool

    @Inject
    protected lateinit var communicationManagerStarter: CommunicationManagerStarter


    fun initializeApp() {
        AppComponent.component.inject(this)

        initialize()

        htmlEditorExtractor.extractHtmlEditorIfNeededAsync()

        htmlEditorExtractor.addHtmlEditorExtractedListener {
            searchEngine.addInitializationListener {
                val currentActivity = activityTracker.currentActivity

                if(currentActivity != null) {
                    preloadHtmlEditors(currentActivity)
                }
                else {
                    activityTracker.addNextActivitySetListener { preloadHtmlEditors(it) }
                }
            }
        }
    }

    private fun initialize() {
        // Apps targeting Android 7.0 (API level 24) and higher do not receive this broadcast if they declare the broadcast receiver in their manifest.
        // Apps will still receive broadcasts if they register their BroadcastReceiver with Context.registerReceiver() and that context is still valid.
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)

        context.registerReceiver(NetworkConnectivityChangeBroadcastReceiver(), intentFilter)
    }

    private fun preloadHtmlEditors(currentActivity: BaseActivity) {
        currentActivity.runOnUiThread { htmlEditorPool.preloadHtmlEditors(currentActivity, 2) }
    }

}