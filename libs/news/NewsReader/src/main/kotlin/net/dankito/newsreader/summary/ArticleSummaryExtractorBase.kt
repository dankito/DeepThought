package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


abstract class ArticleSummaryExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IImplementedArticleSummaryExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleSummaryExtractorBase::class.java)
    }


    private var loadNextItemsUrl: String? = null


    override fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractSummaryAsync(getBaseUrl(), false, callback)
    }

    override fun loadMoreItemsAsync(callback: (AsyncResult<ArticleSummary>) -> Unit) {
        val loadNextItemsUrl = this.loadNextItemsUrl

        if(loadNextItemsUrl == null) {
            callback(AsyncResult(false)) // TODO: add error
        }
        else {
            extractSummaryAsync(loadNextItemsUrl, true, callback)
        }
    }

    private fun extractSummaryAsync(url: String, isForLoadingMoreItems: Boolean, callback: (AsyncResult<ArticleSummary>) -> Unit) {
        thread {
            try {
                callback(AsyncResult(true, result = extractSummary(url, isForLoadingMoreItems)))
            } catch(e: Exception) {
                log.error("Could not get article summary for " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun extractSummary(url: String, isForLoadingMoreItems: Boolean): ArticleSummary {
        requestUrl(url).let { document ->
            val summary = parseHtmlToArticleSummary(url, document, isForLoadingMoreItems)

            loadNextItemsUrl = summary.nextItemsUrl

            return summary
        }
    }

    abstract protected fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary

}