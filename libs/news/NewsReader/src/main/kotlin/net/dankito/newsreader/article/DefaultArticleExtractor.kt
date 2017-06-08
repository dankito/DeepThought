package net.dankito.newsreader.article

import net.dankito.newsreader.model.Article
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.webclient.CookieHandling
import net.dankito.webclient.IWebClient
import net.dankito.webclient.RequestParameters
import net.dankito.webclient.extractor.AsyncResult
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


class DefaultArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultArticleExtractor::class.java)
    }


    override fun extractArticleAsync(item: ArticleSummaryItem, callback: (AsyncResult<Article>) -> Unit) {
        thread {
            try {
                val article = extractArticle(item)
                callback(AsyncResult(true, result = article))
            } catch(e: Exception) {
                log.error("Could not extract Article", e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun extractArticle(item: ArticleSummaryItem) : Article {
        extractContent(item).let { content ->
            return Article(item.url, item.title, content, item.summary, item.publishedDate, item.previewImageUrl)
        }
    }

    private fun extractContent(item: ArticleSummaryItem) : String {
        requestUrl(item.url).let { document ->
            var content = ArticleTextExtractor.extractContent(document, item.summary)

            if(content == null) {
                content = document.body().toString()
            }

            return content
        }
    }

    override fun createParametersForUrl(url: String): RequestParameters {
        val parameters = super.createParametersForUrl(url)

        parameters.cookieHandling = CookieHandling.ACCEPT_ALL_ONLY_FOR_THIS_CALL // some site like New York Times require that cookies are enabled

        return parameters
    }

    override fun parseHtmlToArticle(document: Document, url: String): Article? {
        return null // will not be called in this case
    }

}