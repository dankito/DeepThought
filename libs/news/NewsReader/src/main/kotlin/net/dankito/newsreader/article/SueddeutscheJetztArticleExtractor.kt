package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
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

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "jetzt.de/")
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        // TODO: may re-add extracting old version
        parseHtmlToItemNewVersion(extractionResult, url, document)
    }


    /*        Parsing an SZ Jetzt article of new Homepage Style, introduced beginning 2016      */

    private fun parseHtmlToItemNewVersion(extractionResult: ItemExtractionResult, articleUrl: String, document: Document) {
        document.body().select("article").first()?.let { articleElement ->
            articleElement.select(".article__content").first()?.let { articleContentElement ->
                val content = extractSummary(articleElement) + parseContent(articleContentElement)

                val item = Item(content)
                val source = extractSource(articleElement, articleUrl)

                extractionResult.setExtractedContent(item, source)
            }
        }
    }

    private fun extractSummary(articleElement: Element): String {
        return articleElement.select("div.article__header-teaser").first()?.outerHtml() ?: ""
    }

    private fun parseContent(articleContentElement: Element): String {
        var content = ""

        // data-type=html and iframe e.g. for WhatsApp Kolumne
        articleContentElement.select(".apos-item[data-type=\"richText\"], .apos-item[data-type=\"html\"], .apos-rich-text, .apos-slideshow").forEach { itemsContainer ->
            itemsContainer.select("p, h3, div.apos-slideshow, iframe").forEach { paragraph ->
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


    private fun extractSource(articleElement: Element, articleUrl: String): Source? {
        val title = extractTitle(articleElement)
        val publishingDate = extractPublishingDate(articleElement)

        if(title != null && publishingDate != null) {
            val articleSource = Source(title, articleUrl, publishingDate)

            return articleSource
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
