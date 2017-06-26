package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


abstract class ArticleExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleExtractorBase::class.java)
    }


    override fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        extractArticleAsync(item.url) { extractionResult ->
            extractionResult.result?.entry?.let { entry ->
                if(entry.previewImageUrl == null) {
                    entry.previewImageUrl = item.previewImageUrl
                }
            }

            callback(extractionResult)
        }
    }

    override fun extractArticleAsync(url : String, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        thread {
            try {
                val extractionResult = extractArticle(url)
                extractionResult?.reference?.url = url // explicitly set reference's url as for multipage articles article may gets extracted from a url different than url parameter
                callback(AsyncResult(true, result = extractionResult))
            } catch(e: Exception) {
                log.error("Could not get article for " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    protected open fun extractArticle(url: String): EntryExtractionResult? {
        try {
            requestUrl(url).let { document ->
                return parseHtmlToArticle(document, url)
            }
        } catch (e: Exception) {
            log.error("Could not extract article from " + url, e)
        }

        return null
    }

    abstract protected fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult?


    protected fun makeLinksAbsolute(element: Element, url: String) {
        element.select("a").forEach { anchor ->
            anchor.attr("href", makeLinkAbsolute(anchor.attr("href"), url))
        }

        element.select("img").forEach { anchor ->
            anchor.attr("src", makeLinkAbsolute(anchor.attr("src"), url))
        }
    }

}