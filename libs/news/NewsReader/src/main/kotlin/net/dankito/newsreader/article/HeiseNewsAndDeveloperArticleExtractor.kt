package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.Article
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


    override fun parseHtmlToArticle(document: Document, url: String): Article? {
        document.body().select("article").first()?.let { article ->
            article.select("header").first()?.let { header ->
                header.select(".article__heading").first()?.text()?.let { title ->
                    return parseArticle(header, article, url, title)
                }
            }
        }

        return null
    }

    private fun parseArticle(header: Element, article: Element, url: String, title: String) : Article? {
        article.select(".meldung_wrapper").first()?.let { articleElement ->
            val abstract = articleElement.select(".meldung_anrisstext").first()?.text()
            val previewImageUrl = makeLinkAbsolute(articleElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)
            val publishingDate = extractPublishingDate(header)

            val content = extractContent(article, url)

            return Article(url, title, content, abstract, publishingDate, previewImageUrl)
        }

        return null
    }

    private fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".meldung_wrapper").first()?.children()!!.filter { element ->
            element.hasClass("meldung_anrisstext") == false && containsOnlyComment(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
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