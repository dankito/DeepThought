package net.dankito.deepthought.android.service

import android.content.Intent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.web.UrlUtil


class IntentHandler(private val extractArticleHandler: ExtractArticleHandler, private val router: IRouter, private val urlUtil: UrlUtil) {

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
        if("text/plain" == type) {
            handleReceivedPlainText(intent)
        }
        else if("text/html" == type) {
            handleReceivedText(intent)
        }
    }

    private fun handleReceivedPlainText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
            val trimmedText = sharedText.trim() // K9 Mail sometimes adds empty lines at the end

            if(urlUtil.isHttpUri(trimmedText)) {
                extractArticleHandler.extractAndShowArticleUserDidSeeBefore(trimmedText)
            }
            else {
                handleReceivedText(intent, sharedText)
            }
        }
    }

    private fun handleReceivedText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
            handleReceivedText(intent, sharedText)
        }
    }

    private fun handleReceivedText(intent: Intent, sharedText: String) {
        var sourceTitle: String? = intent.getStringExtra(Intent.EXTRA_SUBJECT) // e.g. Firefox also sends Page Title // TODO: shouldn't it then be used as source title?
        if(sourceTitle == null && intent.hasExtra(Intent.EXTRA_TITLE)) {
            sourceTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
        }

        val source = if (sourceTitle != null) Source(sourceTitle) else null
        val extractionResult = ItemExtractionResult(Item("<p>$sharedText</p>"), source, couldExtractContent = true)

        router.showEditItemView(extractionResult)
    }


    private fun handleActionSendMultipleIntent() {
        //            if (type.startsWith("image/")) {
        //                handleReceivedMultipleImages(intent)
        //            }
    }


}