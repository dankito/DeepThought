package net.dankito.newsreader.article

import net.dankito.newsreader.model.Article
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


abstract class ArticleExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleExtractorBase::class.java)
    }


    override fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<Article>) -> Unit) {
        extractArticleAsync(item.url, callback)
    }

    override fun extractArticleAsync(url : String, callback: (AsyncResult<Article>) -> Unit) {
        thread {
            try {
                callback(AsyncResult(true, result = extractArticle(url)))
            } catch(e: Exception) {
                log.error("Could not get article for " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun extractArticle(url: String): Article? {
        try {
            requestUrl(url).let { document ->
                return parseHtmlToArticle(document, url)
            }
        } catch (e: Exception) {
            log.error("Could not extract article from " + url, e)
        }

        return null
    }

    abstract protected fun parseHtmlToArticle(document: Document, url: String): Article?


    protected fun makeLinksAbsolute(element: Element, url: String) {
        element.select("a").forEach { anchor ->
            anchor.attr("href", makeLinkAbsolute(anchor.attr("href"), url))
        }

        element.select("img").forEach { anchor ->
            anchor.attr("src", makeLinkAbsolute(anchor.attr("src"), url))
        }
    }

}