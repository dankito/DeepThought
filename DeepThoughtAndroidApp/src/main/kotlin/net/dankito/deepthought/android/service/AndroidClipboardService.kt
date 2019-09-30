package net.dankito.deepthought.android.service

import android.content.Intent
import android.os.Build
import android.support.v7.widget.ShareActionProvider
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.utils.ui.ClipboardServiceBase
import net.dankito.utils.windowregistry.android.ui.extensions.currentActivity
import net.dankito.utils.windowregistry.window.WindowRegistry
import javax.inject.Inject


class AndroidClipboardService : ClipboardServiceBase() {


    @Inject
    protected lateinit var windowRegistry: WindowRegistry


    private var shareActionProvider: ShareActionProvider? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun copyUrlToClipboard(url: String) {
        val shareIntent = createShareIntent()

        shareIntent.putExtra(Intent.EXTRA_TEXT, url)

        share(shareIntent)
    }

    override fun copyItemToClipboard(item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        val shareIntent = createShareIntent()

        addItemToShareIntent(shareIntent, item, tags, source, series)

        share(shareIntent)
    }

    private fun addItemToShareIntent(shareIntent: Intent, item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        val itemString = convertItemToStringForCopyingToClipboard(item, tags, source, series)

        shareIntent.putExtra(Intent.EXTRA_TEXT, itemString)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, item.content)
        }

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.summaryPlainText)

        source?.let {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, it.getPreviewWithSeriesAndPublishingDate(series))
        }
    }

    override fun copyItemContentAsHtmlToClipboard(item: Item) {
        val shareIntent = createShareIntent("text/html")

        shareIntent.putExtra(Intent.EXTRA_TEXT, item.content)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, item.content)
        }

        share(shareIntent)
    }


    private fun createShareIntent(type: String = "text/plain"): Intent {
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = type

        return shareIntent
    }

    private fun share(shareIntent: Intent) {
        shareActionProvider?.setShareIntent(shareIntent)

        windowRegistry.currentActivity?.startActivity(shareIntent)
    }

}