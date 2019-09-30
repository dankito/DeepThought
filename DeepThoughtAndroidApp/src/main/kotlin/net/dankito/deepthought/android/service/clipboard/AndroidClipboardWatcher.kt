package net.dankito.deepthought.android.service.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.SnackbarService
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import net.dankito.deepthought.service.data.DataManager
import net.dankito.utils.android.clipboard.AndroidClipboardContent
import net.dankito.utils.android.ui.activities.AppLifeCycleListener
import net.dankito.utils.clipboard.OptionsForClipboardContent
import net.dankito.utils.web.UrlUtil
import net.dankito.utils.windowregistry.android.ui.extensions.asActivity
import net.dankito.utils.windowregistry.android.ui.extensions.currentActivity
import net.dankito.utils.windowregistry.window.WindowRegistry
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AndroidClipboardWatcher(dataManager: DataManager)  {

    companion object {
        private val log = LoggerFactory.getLogger(AndroidClipboardWatcher::class.java)
    }


    @Inject
    protected lateinit var lifeCycleListener: AppLifeCycleListener

    @Inject
    protected lateinit var windowRegistry: WindowRegistry

    @Inject
    protected lateinit var snackbarService: SnackbarService

    @Inject
    protected lateinit var optionsDetector: OptionsForClipboardContentDetector

    @Inject
    protected lateinit var urlUtil: UrlUtil


    private var lastSnackbarShownForUrl: String? = null


    private val activityResumedListener: (Activity) -> Unit = {
        checkIfClipboardContainsUrlOnUiThread(it)
    }


    init {
        dataManager.addInitializationListener { // wait till data loaded otherwise OptionsForClipboardContentDetector -> FileManager -> NetworkSettings throws an exception
            AppComponent.component.inject(this)

            lifeCycleListener.addActivityResumedListener(activityResumedListener)

            windowRegistry.currentActivity?.let { activity ->
                activity.runOnUiThread {
                    checkIfClipboardContainsUrlOnUiThread(activity)
                }
            } ?: run {
                windowRegistry.addNextWindowCreatedListener { window ->
                    window.asActivity()?.let {activity ->
                        activity.runOnUiThread {
                            checkIfClipboardContainsUrlOnUiThread(activity)
                        }
                    }
                }
            }
        }
    }


    private fun checkIfClipboardContainsUrlOnUiThread(currentActivity: Activity) {
        try {
            val clipboardManager = currentActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if(clipboardManager.hasPrimaryClip()) {
                checkIfClipboardContainsUrlOnUiThread(clipboardManager, currentActivity)
            }
        } catch(e: Exception) { log.error("Could not handle Clipboard content", e) }

        lifeCycleListener.addActivityResumedListener(activityResumedListener)
    }

    private fun checkIfClipboardContainsUrlOnUiThread(clipboardManager: ClipboardManager, currentActivity: Activity) {
        val clipItem = clipboardManager.primaryClip.getItemAt(0)

        val clipboardContent = AndroidClipboardContent(clipItem, clipboardManager.primaryClipDescription, urlUtil)

        if(clipboardContent.url != lastSnackbarShownForUrl) {
            lastSnackbarShownForUrl = clipboardContent.url

            optionsDetector.getOptionsAsync(clipboardContent) { options ->
                showClipboardContentOptions(options, currentActivity)
            }
        }
    }

    private fun showClipboardContentOptions(options: OptionsForClipboardContent, activity: Activity) {
        activity.runOnUiThread {
            snackbarService.showClipboardContentOptionsSnackbarOnUiThread(options, activity)
        }
    }

}