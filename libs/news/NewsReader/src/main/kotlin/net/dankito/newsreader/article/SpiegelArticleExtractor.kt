package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SpiegelArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private var SpiegelTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        private val log = LoggerFactory.getLogger(SpiegelArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Spiegel"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.spiegel.de/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().selectFirst("article")?.let { articleElement ->
            val source = extractSource(url, articleElement)

            val content = extractContent(articleElement, url)

            extractionResult.setExtractedContent(Item(content), source)
        }
    }

    private fun extractContent(articleElement: Element, articleUrl: String): String {
        val articleBodyElement = articleElement.selectFirst("[data-area='body']")

        // remove ads and empty nodes
        articleBodyElement.childNodes()
            .filter { shouldRemoveNode(it) }
            .forEach { it.remove() }

        articleElement.selectFirst("[data-area='top_element>image']")?.let { previewImage ->
            articleBodyElement.prependChild(previewImage)
        }

        articleElement.selectFirst(".leading-loose")?.let { summary ->
            articleBodyElement.prependChild(summary)
        }

        unwrapImagesFromNoscriptElementsAndRemoveTheirPictureElements(articleBodyElement)

        articleBodyElement.children().forEach {  // still needed?
            makeLinksAbsolute(it, articleUrl)
        }

        return articleBodyElement.outerHtml()
    }

    private fun shouldRemoveNode(node: Node) =
        if (node is TextNode) {
            node.isBlank // empty text nodes
        } else if (node is Element) {
            node.text().isNullOrBlank() // elements that contain ads
//                    || node.selectFirst("[data-area='related_articles']") != null // remove "Mehr zum Thema" sections
        } else {
            false
        }

    private fun unwrapImagesFromNoscriptElementsAndRemoveTheirPictureElements(element: Element) {
        element.select("noscript > img").forEach { img ->
            img.parent().unwrap() // unwrap img from noscript element

            img.siblingElements() // remove sibling picture element that is only a placeholder
                .filter { it.tagName() == "picture" }
                .forEach { it.remove() }
        }

        element.select("figure button").remove()
    }

    private fun extractSource(articleUrl: String, articleElement: Element): Source? {
        articleElement.selectFirst("header")?.let { headerElement ->
            val articleAriaLabel = articleElement.attr("aria-label")

            val titleAndSubtitleParts = if (articleAriaLabel != null) {
                articleAriaLabel.split(':')
            } else {
                headerElement.select("h2 > span").map { it.text() }
            }
                .map { it.trim() }

            val (title, subtitle) = if (titleAndSubtitleParts.size > 1) {
                titleAndSubtitleParts[1] to titleAndSubtitleParts[0]
            } else {
                titleAndSubtitleParts[0] to null
            }

            val previewImageUrl = articleElement.selectFirst("[data-area='top_element>image'] img")?.attr("src")

            val source = Source(title, articleUrl, previewImageUrl = previewImageUrl, subTitle = subtitle ?: "")

            articleElement.selectFirst("time")?.let { timeElement ->
                source.publishingDate = parseSpiegelTimeFormat(timeElement.attr("datetime"))
            }

            return source
        }

        return null
    }

    private fun parseSpiegelTimeFormat(dateTime: String): Date? {
        try {
            val parsedDate = SpiegelTimeFormat.parse(dateTime)
            return parsedDate
        } catch (ex: Exception) {
            log.error("Could not parse Spiegel Date Format " + dateTime, ex)
        }

        return null
    }

}
