package net.dankito.deepthought.android.service

import android.content.Intent
import net.dankito.deepthought.android.androidservice.PermanentNotificationService
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.UrlUtil


class IntentHandler(private val extractArticleHandler: ExtractArticleHandler, private val router: IRouter, private val urlUtil: UrlUtil, private val permanentNotificationService: PermanentNotificationService) {

    fun handle(intent: Intent) {
        val action = intent.action
        val type = intent.type

        if(Intent.ACTION_SEND == action && type != null) {
            handleActionSendIntent(type, intent)
        }
        else if(Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            handleActionSendMultipleIntent()
        }
        else if(permanentNotificationService.handlesIntent(intent)) {

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
            val trimmedText = sharedText.trim() // K9 Mail sometimes add empty lines at the end
            if(urlUtil.isHttpUri(trimmedText)) {
                extractArticleHandler.extractArticle(trimmedText)
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
        var abstractPlain: String? = intent.getStringExtra(Intent.EXTRA_SUBJECT) // e.g. Firefox also sends Page Title
        if(abstractPlain == null && intent.hasExtra(Intent.EXTRA_TITLE)) {
            abstractPlain = intent.getStringExtra(Intent.EXTRA_TITLE)
        }

        router.showEditEntryView(Item("<p>$sharedText</p>", "<p>$abstractPlain</p>"))
    }


    private fun handleActionSendMultipleIntent() {
        //            if (type.startsWith("image/")) {
        //                handleReceivedMultipleImages(intent)
        //            }
    }


}