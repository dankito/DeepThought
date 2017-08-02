package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
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
        return url.startsWith("https://netzpolitik.org/") && url.length > "https://netzpolitik.org/".length
    }


    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("article").first()?.let { articleElement ->
            val title = articleElement.select(".entry-title").first()?.text() ?: ""
            val reference = Reference(url, title, parsePublishingDate(articleElement))
            reference.previewImageUrl = articleElement.select("figure img").first()?.attr("src")

            val abstract = articleElement.select(".entry-excerpt").first()?.html() ?: ""

            val entry = Entry(extractContent(articleElement), abstract)

            return EntryExtractionResult(entry, reference)
        }

        return null
    }

    private fun extractContent(articleElement: Element): String {
        articleElement.select(".entry-content").first()?.let { contentElement ->
            contentElement.select(".netzpolitik-cta").remove()
            contentElement.select(".vgwort").remove()

            val previewImageHtml = articleElement.select("figure").first()?.outerHtml() ?: ""

            return previewImageHtml + contentElement.outerHtml()
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