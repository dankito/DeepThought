package net.dankito.deepthought.android.service

import android.content.Intent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.SourceSearch
import net.dankito.utils.web.UrlUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class IntentHandler(private val extractArticleHandler: ExtractArticleHandler, private val searchEngine: ISearchEngine,
                    private val router: IRouter, private val urlUtil: UrlUtil) {


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
            val extractedUrl = urlUtil.extractHttpUri(trimmedText) // some app like New York Times and SZ send article title and uri in EXTRA_TEXT -> extract article uri

            if(urlUtil.isHttpUri(trimmedText)) {
                extractArticleHandler.extractAndShowArticleUserDidSeeBefore(trimmedText)
            }
            else if(extractedUrl != null && urlUtil.isHttpUri(extractedUrl) &&
                    trimmedText.endsWith(extractedUrl) && (trimmedText.length - extractedUrl.length < 100)) { // check if shared text has structure <article title> <article url>
                extractArticleHandler.extractAndShowArticleUserDidSeeBefore(extractedUrl)
            }
            else {
                handleReceivedText(intent, trimmedText)
            }
        }
    }

    private fun handleReceivedText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
            handleReceivedText(intent, sharedText)
        }
    }

    private fun handleReceivedText(intent: Intent, sharedText: String) {
        val source = getSource(intent)
        val extractionResult = ItemExtractionResult(Item("<p>$sharedText</p>"), source, couldExtractContent = true)

        router.showEditItemView(extractionResult)
    }

    private fun getSource(intent: Intent): Source? {
        var sourceTitle: String? = intent.getStringExtra(Intent.EXTRA_SUBJECT) // e.g. Firefox also sends Page Title // TODO: shouldn't it then be used as source title?
        if (sourceTitle == null && intent.hasExtra(Intent.EXTRA_TITLE)) {
            sourceTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
        }

        if (sourceTitle != null) {
            val adjustedSourceTitle = mayAdjustSourceTitle(sourceTitle)
            val existingSource = findExistingSource(adjustedSourceTitle)

            return existingSource ?: Source(adjustedSourceTitle)
        }

        return null
    }

    private fun mayAdjustSourceTitle(sourceTitle: String): String {
        return when {
            // Xodo PDF reader adds 'Selected text from: ' before file name (was Basti's request to remove it)
            sourceTitle.startsWith("Selected text from: ") -> sourceTitle.substring("Selected text from: ".length)
            sourceTitle.startsWith("Selektierter Text von: ") -> sourceTitle.substring("Selektierter Text von: ".length)
            else -> sourceTitle
        }
    }

    private fun findExistingSource(sourceTitle: String): Source? {
        val foundSource = AtomicReference<Source>(null)
        val countDownLatch = CountDownLatch(1)

        searchEngine.searchSources(SourceSearch(sourceTitle) { searchResult ->
            if (searchResult.isNotEmpty()) {
                foundSource.set(searchResult.first())
            }

            countDownLatch.count
        })

        try { countDownLatch.await(2, TimeUnit.SECONDS) } catch (ignored: Exception) { }

        return foundSource.get()
    }


    private fun handleActionSendMultipleIntent() {
        //            if (type.startsWith("image/")) {
        //                handleReceivedMultipleImages(intent)
        //            }
    }


}