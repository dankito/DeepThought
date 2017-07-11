package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.SimpleDateFormat
import java.util.*


class HeiseNewsAndDeveloperArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val DateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }


    override fun getName(): String? {
        return "Heise"
    }

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

    private fun parseArticle(header: Element, article: Element, url: String, title: String) : EntryExtractionResult? {
        article.select(".meldung_wrapper").first()?.let { articleElement ->
            val entry = Entry(extractContent(article, url))
            articleElement.select(".meldung_anrisstext").first()?.text()?.let { entry.abstractString = it }

            entry.previewImageUrl = makeLinkAbsolute(articleElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)
            val publishingDate = extractPublishingDate(header)
            val reference = Reference(url, title, publishingDate, getName())

            return EntryExtractionResult(entry, reference)
        }

        return null
    }

    private fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".meldung_wrapper").first()?.children()!!.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
    }

    private fun shouldFilterElement(element: Element): Boolean {
        return element.hasClass("meldung_anrisstext") || element.hasClass("widget-werbung") || containsOnlyComment(element)
    }

    private fun containsOnlyComment(element: Element) : Boolean {
        return element.childNodeSize() == 3 && element.childNode(1) is Comment && element.childNode(0) is TextNode && element.childNode(2) is TextNode
    }

    private fun getContentElementHtml(element: Element, url: String) : String {
        makeLinksAbsolute(element, url)
        return element.outerHtml()
    }

    private fun extractPublishingDate(header: Element): Date? {
        header.select(".publish-info time").first()?.let {
            return DateTimeFormat.parse(it.attr("datetime"))
        }

        return null
    }

}