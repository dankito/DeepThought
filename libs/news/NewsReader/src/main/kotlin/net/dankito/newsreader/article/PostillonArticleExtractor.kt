package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


class PostillonArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val PostillionDateFormat = SimpleDateFormat("d.M.yy", Locale.GERMAN)

        private val log = LoggerFactory.getLogger(PostillonArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Postillon"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.der-postillon.com/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().selectFirst(".post, .blog-post")?.let { postElement ->
            postElement.selectFirst(".post-title, .entry-title")?.let { titleElement ->
                postElement.selectFirst(".post-body")?.let { bodyElement ->
                    val item = Item(extractContent(bodyElement))

                    val source = Source(titleElement.text(), url, extractPublishingDate(postElement))

                    bodyElement.select(".separator a img").first()?.let { source.previewImageUrl = it.attr("src") }

                    extractionResult.setExtractedContent(item, source)
                }
            }
        }
    }

    private fun extractContent(bodyElement: Element): String {
        bodyElement.select("img").forEach { it.attr("width", "100%") }

        return bodyElement.childNodes().filter { shouldFilterNode(it) == false }.joinToString(separator = "") { getTextRepresentationForContentNode(it) }
    }

    private fun getTextRepresentationForContentNode(node: Node): CharSequence {
        return if ("br" == node.nodeName()) {
            "</p><p>"
        }
        else if (node is TextNode) {
            node.text()
        }
        else if ("div" == node.nodeName() && (node as Element).hasClass("separator")) {
            node.outerHtml() + "<p>"
        }
        else if(isSonntagsfrageElement(node)) {
            extractSonntagsfrageHtml(node as Element)
        }
        else {
            node.outerHtml()
        }
    }

    private fun isSonntagsfrageElement(node: Node): Boolean {
        return node is Element && node.text().contains("(Direktlink zur Umfrage)")
    }

    private fun extractSonntagsfrageHtml(sonntagsFrageElement: Element): String {
        sonntagsFrageElement.selectFirst("noscript a")?.let { noscriptElement ->
            val pollDaddyLink = noscriptElement.attr("href")

            val iframe = Element("iframe")
            iframe.attr("src", pollDaddyLink)
            iframe.attr("height", "600")
            iframe.attr("width", "100%")

            noscriptElement.parentNode().replaceWith(iframe)

            // remove script element that loads PollDaddy JS
            sonntagsFrageElement.select("script").remove()

            return sonntagsFrageElement.html()
        }

        return ""
    }

    private fun shouldFilterNode(node: Node) : Boolean {
        return (node is TextNode && node.isBlank) || node.attr("id") == "narrando_mobil" || ("a" == node.nodeName() && "more" == (node as Element).attr("name"))
    }

    private fun extractPublishingDate(postElement: Element): Date? {
        postElement.selectFirst(".entry-time time")?.let {
            try {
                return PostillionDateFormat.parse(it.text())
            } catch (e: Exception) {
                log.warn("Could not parse Postillon date '${it.text()}'", e)
            }
        }

        return null
    }

}