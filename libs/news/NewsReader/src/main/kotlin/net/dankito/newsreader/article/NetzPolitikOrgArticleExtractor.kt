package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


class NetzPolitikOrgArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val DateFormat = SimpleDateFormat("dd.MM.yyyy")

        private val log = LoggerFactory.getLogger(NetzPolitikOrgArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "netzpolitik.org"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "netzpolitik.org/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            val title = articleElement.select(".entry-title").first()?.text() ?: ""
            val reference = Source(title, url, parsePublishingDate(articleElement), articleElement.select("figure img").first()?.attr("src"))

            val entry = Item(extractContent(articleElement))

            extractionResult.setExtractedContent(entry, reference)
        }
    }

    private fun extractContent(articleElement: Element): String {
        articleElement.select(".entry-content").first()?.let { contentElement ->
            contentElement.select(".netzpolitik-cta").remove()
            contentElement.select(".vgwort").remove()

            val previewImageHtml = articleElement.select("figure").first()?.outerHtml() ?: ""
            val abstract = articleElement.select(".entry-excerpt").first()?.html() ?: ""

            return previewImageHtml + abstract + contentElement.outerHtml()
        }

        log.error("Could not find element with class 'entry-content'")
        return ""
    }

    private fun parsePublishingDate(articleElement: Element): Date? {
        articleElement.select("time.published").first()?.let { timeElement ->
            val dateString = timeElement.text().trim()

            try {
                return DateFormat.parse(dateString)
            } catch(e: Exception) { log.error("Could not parse netzpolitik.org date string $dateString", e) }
        }

        return null
    }

}