package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.thread


abstract class ArticleExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleExtractorBase::class.java)
    }


    override fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        extractArticleAsync(item.url) { extractionResult ->
            extractionResult.result?.reference?.let { reference ->
                if(reference.previewImageUrl == null) {
                    reference.previewImageUrl = item.previewImageUrl
                }
            }

            callback(extractionResult)
        }
    }

    override fun extractArticleAsync(url : String, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        thread {
            try {
                val extractionResult = extractArticle(url)

                if(extractionResult != null) {
                    extractionResult.reference?.url = url // explicitly set reference's url as for multipage articles article may gets extracted from a url different than url parameter
                    extractionResult.reference?.lastAccessDate = Date()

                    callback(AsyncResult(true, result = extractionResult))
                }
                else {
                    callback(AsyncResult(false, Exception("Could not extract article from url $url"))) // TODO: localize
                }
            } catch(e: Exception) {
                log.error("Could not extract article from " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    protected open fun extractArticle(url: String): EntryExtractionResult? {
        requestUrl(url).let { document ->
            return parseHtmlToArticle(document, url)
        }
    }

    protected open fun extractArticleWithPost(url: String, body: String? = null): EntryExtractionResult? {
        try {
            requestUrlWithPost(url, body).let { document ->
                return parseHtmlToArticle(document, url)
            }
        } catch (e: Exception) {
            log.error("Could not extract article with post from " + url, e)
        }

        return null
    }

    abstract protected fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult?


    protected fun makeLinksAbsolute(element: Element, url: String) {
        element.select("[href]").forEach { hrefElement ->
            hrefElement.attr("href", makeLinkAbsolute(hrefElement.attr("href"), url))
        }

        element.select("[src]").forEach { srcElement ->
            srcElement.attr("src", makeLinkAbsolute(srcElement.attr("src"), url))
        }

        element.select("[data-zoom-src]").forEach { srcElement ->
            srcElement.attr("data-zoom-src", makeLinkAbsolute(srcElement.attr("data-zoom-src"), url))
        }
    }


    protected fun adjustSourceElements(element: Element) {
        for (sourceElement in element.select("span.source")) {
            sourceElement.parent().appendChild(Element(org.jsoup.parser.Tag.valueOf("br"), element.baseUri()))
        }
    }

}