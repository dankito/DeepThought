package net.dankito.deepthought.android.service

import android.app.Activity
import android.content.*
import android.os.Build
import android.support.v7.widget.ShareActionProvider
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.utils.UrlUtil
import net.dankito.utils.ui.IClipboardService
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AndroidClipboardService : IClipboardService {

    companion object {
        private val log = LoggerFactory.getLogger(AndroidClipboardService::class.java)
    }


    @Inject
    protected lateinit var activityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var lifeCycleListener: AppLifeCycleListener

    @Inject
    protected lateinit var snackbarService: SnackbarService

    @Inject
    protected lateinit var extractArticleHandler: ExtractArticleHandler

    @Inject
    protected lateinit var urlUtil: UrlUtil


    private var shareActionProvider: ShareActionProvider? = null

    private var lastSnackbarShownForUrl: String? = null


    private val activityResumedListener: (Activity) -> Unit = {
        checkIfClipboardContainsUrl(it)
    }


    init {
        AppComponent.component.inject(this)

        lifeCycleListener.addActivityResumedListener(activityResumedListener)
    }


    override fun copyUrlToClipboard(url: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND

        shareIntent.putExtra(Intent.EXTRA_TEXT, url)

        shareIntent.type = "text/plain"

        share(shareIntent)
    }

    override fun copyEntryToClipboard(item: Item, source: Source?, series: Series?) {
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"

        addItemToShareIntent(shareIntent, item, source, series)

        share(shareIntent)
    }

    private fun addItemToShareIntent(shareIntent: Intent, item: Item, source: Source?, series: Series?) {
        var content = item.contentPlainText

        if(source != null) {
            content = "$content\n\n(${source.getPreviewWithSeriesAndPublishingDate(series)}: ${source.url})"
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, content)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, item.content)
        }

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.abstractPlainText)
    }

    private fun share(shareIntent: Intent) {
        shareActionProvider?.setShareIntent(shareIntent)

        activityTracker.currentActivity?.startActivity(shareIntent)
    }


    private fun checkIfClipboardContainsUrl(currentActivity: Activity) {
        try {
            val clipboardManager = currentActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if(clipboardManager.hasPrimaryClip()) {
                checkIfClipboardContainsUrl(clipboardManager, currentActivity)
            }
        } catch(e: Exception) { log.error("Could not handle Clipboard content", e) }

        lifeCycleListener.addActivityResumedListener(activityResumedListener)
    }

    private fun checkIfClipboardContainsUrl(clipboardManager: ClipboardManager, currentActivity: Activity) {
        val clipItem = clipboardManager.primaryClip.getItemAt(0)
        val description = clipboardManager.primaryClipDescription

        getUrlFromClipItem(clipItem, description)?.let { url ->
            if(url != lastSnackbarShownForUrl) {
                lastSnackbarShownForUrl = url
                snackbarService.showUrlInClipboardDetectedSnackbar(currentActivity, url) { extractArticleHandler.extractArticle(url) }
            }
        }
    }

    private fun getUrlFromClipItem(item: ClipData.Item, description: ClipDescription): String? {
        if(description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            val text = item.text.toString()
            if(urlUtil.isHttpUri(text)) {
                return text
            }
        }
        else if(description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST)) {
            return item.uri.toString()
        }

        return null
    }

}