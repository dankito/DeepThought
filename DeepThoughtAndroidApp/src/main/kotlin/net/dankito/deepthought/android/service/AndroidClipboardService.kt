package net.dankito.deepthought.android.service

import android.content.Intent
import android.os.Build
import android.support.v7.widget.ShareActionProvider
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.utils.ui.ClipboardServiceBase
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AndroidClipboardService : ClipboardServiceBase() {

    companion object {
        private val log = LoggerFactory.getLogger(AndroidClipboardService::class.java)
    }


    @Inject
    protected lateinit var activityTracker: CurrentActivityTracker


    private var shareActionProvider: ShareActionProvider? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun copyUrlToClipboard(url: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND

        shareIntent.putExtra(Intent.EXTRA_TEXT, url)

        shareIntent.type = "text/plain"

        share(shareIntent)
    }

    override fun copyItemToClipboard(item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"

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
    }

    private fun share(shareIntent: Intent) {
        shareActionProvider?.setShareIntent(shareIntent)

        activityTracker.currentActivity?.startActivity(shareIntent)
    }

}