package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.CookieHandling
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


class DefaultArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultArticleExtractor::class.java)
    }


    override fun extractArticleAsync(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        thread {
            try {
                val extractionResult = extractArticle(item)
                callback(AsyncResult(true, result = extractionResult))
            } catch(e: Exception) {
                log.error("Could not extract Article", e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun extractArticle(item: ArticleSummaryItem) : EntryExtractionResult {
        extractContent(item).let { content ->
            val entry = Entry(content, item.summary)
            // TODO: handle item.previewImageUrl
            val reference = Reference(item.url, item.title, item.publishedDate)

            return EntryExtractionResult(entry, reference)
        }
    }

    private fun extractContent(item: ArticleSummaryItem) : String {
        requestUrl(item.url).let { document ->
            return extractContent(document, item.summary)
        }
    }

    override fun createParametersForUrl(url: String): RequestParameters {
        val parameters = super.createParametersForUrl(url)

        parameters.cookieHandling = CookieHandling.ACCEPT_ALL_ONLY_FOR_THIS_CALL // some site like New York Times require that cookies are enabled

        return parameters
    }

    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        return null // will not be called in this case
    }


    private fun extractContent(document: Document, contentIndicator: String): String {
        var content = ArticleTextExtractor.extractContent(document, contentIndicator)

        if (content == null) {
            content = document.body().toString()
        }

        return content
    }

}