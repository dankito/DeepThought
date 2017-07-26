package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.SimpleDateFormat
import java.util.*


abstract class HeiseNewsAndDeveloperArticleExtractorBase(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val DateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }


    abstract protected fun parseArticle(header: Element, article: Element, url: String, title: String) : EntryExtractionResult?


    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("article").first()?.let { article ->
            getReadAllOnOnePageUrl(article, url)?.let { allOnOnePageUrl ->
                return extractArticle(allOnOnePageUrl)
            }

            article.select("header").first()?.let { header ->
                header.select(".article__heading").first()?.text()?.let { title ->
                    return parseArticle(header, article, url, title)
                }
            }
        }

        return null
    }

    private fun getReadAllOnOnePageUrl(article: Element, siteUrl: String): String? {
        article.select(".pre-akwa-toc__item--onepage a.pre-akwa-toc__link").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        return null
    }


    protected open fun containsOnlyComment(element: Element) : Boolean {
        return element.childNodeSize() == 3 && element.childNode(1) is Comment && element.childNode(0) is TextNode && element.childNode(2) is TextNode
    }

    protected open fun getContentElementHtml(element: Element, url: String) : String {
        makeLinksAbsolute(element, url)
        return element.outerHtml()
    }


    protected fun extractPublishingDate(header: Element): Date? {
        header.select("time").first()?.let { dateTimeElement ->
            return DateTimeFormat.parse(dateTimeElement.attr("datetime"))
        }

        return null
    }

}