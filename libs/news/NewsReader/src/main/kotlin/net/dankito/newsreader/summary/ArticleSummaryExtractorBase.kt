package net.dankito.newsreader.summary

import net.dankito.newsreader.ExtractorBase
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.util.AsyncResult
import net.dankito.util.web.IWebClient
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


abstract class ArticleSummaryExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IImplementedArticleSummaryExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleSummaryExtractorBase::class.java)
    }


    override fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractSummaryAsync(getUrl(), false, callback)
    }

    override fun loadMoreItemsAsync(articleSummary: ArticleSummary, callback: (AsyncResult<ArticleSummary>) -> Unit) {
        val loadNextItemsUrl = articleSummary.nextItemsUrl

        if(loadNextItemsUrl == null) {
            callback(AsyncResult(false)) // TODO: add error
        }
        else {
            extractSummaryAsync(loadNextItemsUrl, true) { result ->
                nextItemsLoaded(articleSummary, result, callback)
            }
        }
    }

    private fun nextItemsLoaded(articleSummary: ArticleSummary, result: AsyncResult<ArticleSummary>, callback: (AsyncResult<ArticleSummary>) -> Unit) {
        val extractedSummary = result.result

        if (extractedSummary != null) {
            articleSummary.nextItemsLoaded(extractedSummary)
            callback(AsyncResult(true, result = articleSummary))
        } else {
            callback(result)
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
            return parseHtmlToArticleSummary(url, document, isForLoadingMoreItems)
        }
    }

    abstract protected fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary

}