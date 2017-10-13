package net.dankito.deepthought.android.service

import android.content.Intent
import android.os.Build
import android.support.v7.widget.ShareActionProvider
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series
import net.dankito.utils.ui.IClipboardService


class AndroidClipboardService(private val activityTracker: CurrentActivityTracker) : IClipboardService {

    private var shareActionProvider: ShareActionProvider? = null


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

        var content = item.contentPlainText

        if(source != null) { // TODO: Replace System.lineSeparator by PlatformConfig.getNewLineChar()
            content = content + System.lineSeparator() + System.lineSeparator() + "(" + source.getPreviewWithSeriesAndPublishingDate(series) + ": " + source.url + ")"
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, content)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, item.content)
        }

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.abstractPlainText)

        shareIntent.type = "text/plain"

        share(shareIntent)
    }

    private fun share(shareIntent: Intent) {
        shareActionProvider?.setShareIntent(shareIntent)

        activityTracker.currentActivity?.startActivity(shareIntent)
    }
}