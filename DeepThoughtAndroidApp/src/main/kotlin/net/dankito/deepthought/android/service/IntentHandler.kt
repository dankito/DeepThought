package net.dankito.deepthought.android.service

import android.content.Intent
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.utils.ui.IDialogService
import java.net.URI


class IntentHandler(private val articleExtractors: ArticleExtractors, private val router: IRouter, private val dialogService: IDialogService) {

    fun handle(intent: Intent) {
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            handleActionSendIntent(type, intent)
        }
        else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            handleActionSendMultipleIntent()
        }
    }


    private fun handleActionSendIntent(type: String?, intent: Intent) {
        if ("text/plain" == type) {
            handleReceivedPlainText(intent)
        }
//            else if ("text/html" == type) {
//                handleReceivedHtmlText(intent)
//            }
    }

    private fun handleReceivedPlainText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            if (isReceivedTextAnUri(sharedText)) {
                articleExtractors.extractArticleAsync(sharedText) {
                    it.result?.let { router.showViewEntryView(it) }
                    it.error?.let { showErrorMessage(it) }
                }
            }
            else {
                var abstractPlain: String? = intent.getStringExtra(Intent.EXTRA_SUBJECT) // e.g. Firefox also sends Page Title
                if (abstractPlain == null && intent.hasExtra(Intent.EXTRA_TITLE)) {
                    abstractPlain = intent.getStringExtra(Intent.EXTRA_TITLE)
                }

//                showEditEntryDialogForReceivedData("<p>$sharedText</p>", "<p>$abstractPlain</p>")
            }
        }
    }

    private fun isReceivedTextAnUri(sharedText: String): Boolean {
        try {
            val uri = URI.create(sharedText)
            return uri != null
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }


    private fun handleActionSendMultipleIntent() {
        //            if (type.startsWith("image/")) {
        //                handleReceivedMultipleImages(intent)
        //            }
    }


    private fun showErrorMessage(error: Exception) {
        dialogService.showErrorMessage(dialogService.getLocalization().getLocalizedString("alert.message.could.not.extract.entry.from.url"), exception = error)
    }

}