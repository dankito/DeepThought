package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SueddeutscheJetztArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val sueddeutscheJetztDateTimeFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy")

        private val log = LoggerFactory.getLogger(SueddeutscheJetztArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "SZ Jetzt"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return (url.toLowerCase().startsWith("http://www.jetzt.de/") && url.length > "http://www.jetzt.de/".length) ||
                (url.toLowerCase().startsWith("http://jetzt.sueddeutsche.de/") && url.length > "http://jetzt.sueddeutsche.de/".length)
    }

    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        // TODO: may re-add extracting old version
        return parseHtmlToEntryNewVersion(url, document)
    }


    /*        Parsing an SZ Jetzt article of new Homepage Style, introduced beginning 2016      */

    private fun parseHtmlToEntryNewVersion(articleUrl: String, document: Document): EntryExtractionResult? {
        document.body().select("article").first()?.let { articleElement ->
            articleElement.select(".article__content").first()?.let { articleContentElement ->
                val content = parseContent(articleContentElement)
                val abstractString = extractAbstract(articleElement)

                val entry = Entry(content, abstractString)
                val reference = extractReference( articleElement, articleUrl)

                val extractionResult = EntryExtractionResult(entry, reference)

                return extractionResult
            }
        }
        
        return null
    }

    private fun extractAbstract(articleElement: Element): String {
        articleElement.select("div.article__header-teaser").first()?.let { teaserElement ->
            return teaserElement.html()
        }
        
        return ""
    }

    private fun parseContent(articleContentElement: Element): String {
        var content = ""

        // data-type=html and iframe e.g. for WhatsApp Kolumne
        articleContentElement.select(".apos-item[data-type=\"richText\"], .apos-item[data-type=\"html\"]").forEach { itemsContainer ->
            itemsContainer.select("p, h3, iframe").forEach { paragraph ->
                content += parseParagraph(paragraph)
            }
        }

        return content
    }

    private fun parseParagraph(paragraph: Element): String {
        if(paragraph?.tagName() == "iframe") {
            return paragraph.parent().parent().outerHtml()
        }

        if (isEmptyParagraph(paragraph)) {
            return ""
        }

        return paragraph.outerHtml()
    }

    private fun isEmptyParagraph(paragraph: Element?): Boolean {
        if(paragraph == null || paragraph.text().trim().isBlank()) {
            return true
        }

        val text = paragraph.text().replace(160.toChar(), ' ').replace(8234.toChar(), ' ') // replace non breakable spaces
        return text.trim().isBlank()
    }


    private fun extractReference(articleElement: Element, articleUrl: String): Reference? {
        val title = extractTitle(articleElement)
        val publishingDate = extractPublishingDate(articleElement)

        if(title != null && publishingDate != null) {
            val articleReference = Reference(articleUrl, title, publishingDate = publishingDate)

            return articleReference
        }

        return null
    }

    private fun extractTitle(articleElement: Element): String? {
        var title: String? = null

        val header2Elements = articleElement.getElementsByClass("article__header-title")
        if (header2Elements.size > 0)
            title = header2Elements[0].text()
        else
            log.warn("Could not find h1 child Element of article Element with class 'article__header-title'")

        return title
    }

    private fun extractPublishingDate(articleElement: Element): Date? {
        val headerDateElements = articleElement.getElementsByClass("article__header-date")
        if (headerDateElements.size == 0) {
            log.warn("Could not find Element with class 'article__header-date', therefore cannot extract Article's publishing date")
            return null
        }

        val headerDateElement = headerDateElements[0]
        return tryToParseSueddeutscheJetztPublishingDate(headerDateElement.text())
    }

    private fun tryToParseSueddeutscheJetztPublishingDate(publishingDate: String): Date? {
        var publishingDate = publishingDate
        publishingDate = publishingDate.trim { it <= ' ' }

        try {
            return sueddeutscheJetztDateTimeFormat.parse(publishingDate)
        } catch (ex: Exception) {
            log.error("Could not parse Sueddeutsche Jetzt Date " + publishingDate, ex)
        }

        return null
    }

}
