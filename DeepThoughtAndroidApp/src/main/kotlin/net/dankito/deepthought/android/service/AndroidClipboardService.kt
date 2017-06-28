package net.dankito.deepthought.android.service

import android.content.Intent
import android.os.Build
import android.support.v7.widget.ShareActionProvider
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.extensions.abstractPlainText
import net.dankito.deepthought.extensions.contentPlainText
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.utils.ui.IClipboardService


class AndroidClipboardService(private val activityTracker: CurrentActivityTracker) : IClipboardService {

    private var shareActionProvider: ShareActionProvider? = null


    override fun copyReferenceUrlToClipboard(reference: Reference) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND

        shareIntent.putExtra(Intent.EXTRA_TEXT, reference.url)

        shareIntent.type = "text/plain"

        share(shareIntent)
    }

    override fun copyEntryToClipboard(entry: Entry, reference: Reference?) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND

        var content = entry.contentPlainText

        if(reference != null) { // TODO: Replace System.lineSeparator by PlatformConfig.getNewLineChar()
            content = content + System.lineSeparator() + System.lineSeparator() + "(" + reference.preview + ": " + reference.url + ")"
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, content)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, entry.content)
        }

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.abstractPlainText)

        shareIntent.type = "text/plain"

        share(shareIntent)
    }

    private fun share(shareIntent: Intent) {
        shareActionProvider?.setShareIntent(shareIntent)

        activityTracker.currentActivity?.startActivity(shareIntent)
    }
}