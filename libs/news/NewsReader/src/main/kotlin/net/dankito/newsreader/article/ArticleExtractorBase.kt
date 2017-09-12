package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
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
            extractionResult.result?.let {
                setInfoFromArticleSummaryItemOnExtractionResult(item, it)
            }

            callback(extractionResult)
        }
    }

    private fun setInfoFromArticleSummaryItemOnExtractionResult(item: ArticleSummaryItem, extractionResult: EntryExtractionResult) {
        if(extractionResult.entry.abstractString.isNullOrBlank()) {
            extractionResult.entry.abstractString = item.summary
        }

        extractionResult.reference?.let { reference ->
            if (reference.previewImageUrl == null) {
                reference.previewImageUrl = item.previewImageUrl
            }
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
            val contentHtml = document.outerHtml()
            val extractionResult = EntryExtractionResult(Entry(contentHtml), Reference(url, url, null))

            parseHtmlToArticle(extractionResult, document, url)

            // TODO: if extraction didn't work try default ArticleExtraction if not done already

            return extractionResult
        }
    }

    protected open fun extractArticleWithPost(extractionResult: EntryExtractionResult, url: String, body: String? = null) {
        try {
            requestUrlWithPost(url, body).let { document ->
                parseHtmlToArticle(extractionResult, document, url)
            }
        } catch (e: Exception) {
            extractionResult.error = e
            log.error("Could not extract article with post from " + url, e)
        }
    }

    abstract protected fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String)


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