package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SpiegelArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Spiegel"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.toLowerCase().contains("://www.spiegel.de/") && url.length > "://www.spiegel.de/".length + 4
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
            val contentElement = document.body().getElementById("content-main")

            val articleSectionElements = document.body().getElementsByClass("article-section")
            val articleIntroElements = contentElement.getElementsByClass("article-intro")

            val entry = createEntry(articleSectionElements, articleIntroElements)

            val reference = extractReference(url, contentElement)

            extractionResult.setExtractedContent(entry, reference)

    }

    private fun createEntry(articleSectionElements: Elements, articleIntroElements: Elements): Item {
        val content = extractContentFromArticleSection(articleSectionElements)
        val abstractString = extractAbstractFromArticleIntro(articleIntroElements) ?: ""

        return Item(content, abstractString)
    }

    private fun extractContentFromArticleSection(articleSectionElements: Elements): String {
        for (articleSectionElement in articleSectionElements) {
            if ("div" == articleSectionElement.nodeName()) {
                return extractContentFromArticleSection(articleSectionElement)
            }
        }

        return ""
    }

    private fun extractContentFromArticleSection(articleSectionElement: Element): String {
        var contentHtml = ""

        for (childNode in articleSectionElement.childNodes()) {
            if (childNode is TextNode) {
                val textNode = childNode
                if(textNode.text().trim().isNullOrEmpty() == false) {
                    contentHtml += appendHtml(textNode.text().trim(), childNode)
                }
            }
            else if (childNode is Element) {
                val childElement = childNode
                if ("div" == childElement.nodeName() == false && childElement.text().trim().isNullOrEmpty() == false &&
                        childElement.html().contains("<span class=\"spTextSmaller\">") == false) { // filters 'Wenig Zeit? Am Textende gibt's eine Zusammenfassung.'
                    if ("ul" == childElement.nodeName())
                        contentHtml += "<p>" + childElement.outerHtml() + "</p>"
                    else if ("a" == childElement.nodeName())
                        contentHtml += " " + childElement.outerHtml()
                    else
                    //          if(childElement.outerHtml().contains("href=\"http://boersen.manager-magazin.de/mm/kurse_einzelkurs_suche.") == false) // TODO: filter Boersenkurse
                    // TODO: filter 'Diese Meldung stammt aus dem SPIEGEL'
                        contentHtml += appendHtml(childElement.html(), childNode)
                }
            }
        }

        contentHtml = resolveTextLinks(contentHtml)

        return contentHtml
    }

    private fun appendHtml(html: String, currentNode: Node): String {
        var resultingHtml = ""

        if(currentNode.previousSibling().nodeName() != "a") {
            resultingHtml += "<p>"
        }
        else if(html.isNullOrEmpty() == false && html[0].isLetterOrDigit()) {
            // previous node has been an url. if now a text beginning with an alphanumeric character, add a white space before
            resultingHtml += " "
        }

        resultingHtml += html

        if ("a" == currentNode.nextSibling().nodeName() == false)
            resultingHtml += "</p>"

        return resultingHtml
    }

    private fun resolveTextLinks(contentHtml: String): String {
        return contentHtml.replace("<a href=\"/", "<a href=\"http:www.spiegel.de/")
    }

    private fun extractAbstractFromArticleIntro(articleIntroElements: Elements): String? {
        for (articleIntroElement in articleIntroElements) {
            if ("p" == articleIntroElement.nodeName()) {
                return extractAbstractFromArticleIntro(articleIntroElement)
            }
        }

        return null
    }

    private fun extractAbstractFromArticleIntro(articleIntroElement: Element): String {
        var abstractString = ""
        for (child in articleIntroElement.children()) {
            if ("strong" == child.nodeName())
                abstractString += child.html()
            else
                abstractString += child.html()
        }

        return abstractString
    }

    private fun extractReference(articleUrl: String, contentElement: Element): Source {
        val title = extractTitle(contentElement)
        val subTitle = extractSubTitle(contentElement)

        val publishingDate = extractPublishingDate(contentElement)

        return Source(articleUrl, title, publishingDate, subTitle = subTitle)
    }

    private fun extractTitle(contentElement: Element): String {
        var title = ""
        val headerElements = contentElement.getElementsByClass("headline")
        for (headerElement in headerElements) {
            if ("span" == headerElement.nodeName()) {
                title = headerElement.text()
                break
            }
        }
        return title
    }

    private fun extractSubTitle(contentElement: Element): String {
        var subTitle = ""
        val headerIntroElements = contentElement.getElementsByClass("headline-intro")
        for (headerIntroElement in headerIntroElements) {
            if ("span" == headerIntroElement.nodeName()) {
                subTitle = headerIntroElement.text()
                if (subTitle.endsWith(":"))
                    subTitle = subTitle.substring(0, subTitle.length - 1)
                break
            }
        }
        return subTitle
    }

    private fun extractPublishingDate(contentElement: Element): Date? {
        var publishingDate: Date? = null
        val timeFormatElements = contentElement.getElementsByClass("timeformat")
        for (timeFormatElement in timeFormatElements) {
            if ("time" == timeFormatElement.nodeName() && timeFormatElement.hasAttr("datetime")) {
                val spiegelTimeFormat = timeFormatElement.attr("datetime")
                publishingDate = parseSpiegelTimeFormat(spiegelTimeFormat)
                break
            }
        }

        return publishingDate
    }

    private var spiegelTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private fun parseSpiegelTimeFormat(dateTime: String): Date? {
        try {
            val parsedDate = spiegelTimeFormat.parse(dateTime)
            return parsedDate
        } catch (ex: Exception) {
            log.error("Could not parse Spiegel Date Format " + dateTime, ex)
        }

        return null
    }

    companion object {

        private val log = LoggerFactory.getLogger(SpiegelArticleExtractor::class.java)
    }

}
