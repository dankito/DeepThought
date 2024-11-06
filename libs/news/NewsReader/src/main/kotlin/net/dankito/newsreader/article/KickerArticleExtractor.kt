package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.extensions.attrOrNull
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class KickerArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val kickerDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        private val log = LoggerFactory.getLogger(ArticleExtractorBase::class.java)
    }


    override fun getName() = "Kicker"

    override fun canExtractItemFromUrl(url: String) = isHttpOrHttpsUrlFromHost(url, "www.kicker.de")


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().selectFirst("article")?.let { article ->
            val title = article.selectFirst("header h2")?.text() ?: ""
            val subTitle = article.selectFirst("header > p")?.text() ?: ""

            article.selectFirst(".kick__article__content")?.let { contentElement ->
                contentElement.selectFirst(".kick__article__content__child")?.let { content ->
                    val summary = contentElement.selectFirst(".kick__article__teaser")?.text() ?: ""
                    val previewImage = contentElement.selectFirst(".kick__article__picture--teaser, .kick__article__picture")
                        ?.selectFirst("img")?.attrOrNull("src")
                    val publishingDate = parseDate(document)

                    extractionResult.setExtractedContent(Item(content.outerHtml()), Source(title, url, publishingDate, previewImage, subTitle))
                }
            }
        }

        document.body().selectFirst(".kick__slideshow__view")?.let { slideshowElement ->
            extractSlideshowArticle(extractionResult, document, url, slideshowElement)
        }
    }

    private fun extractSlideshowArticle(extractionResult: ItemExtractionResult, document: Document, url: String, slideshowElement: Element) {
        val title = slideshowElement.selectFirst(".kick__slideshow__view__title")?.text() ?: ""
        val subTitle = slideshowElement.selectFirst(".kick__slideshow__view__subtitle p")?.text() ?: ""
        val previewImage = slideshowElement.selectFirst(".kick__slideshow__view-picture picture img")?.attrOrNull("src")
        val publishingDate = parseDate(document)

        extractionResult.setExtractedContent(Item(slideshowElement.outerHtml()), Source(title, url, publishingDate, previewImage, subTitle))
    }

    private fun parseDate(document: Document): Date? =
        try {
            var dateString = document.head().selectFirst("meta[name='Date']")?.attrOrNull("content")
            if (dateString != null) {
                val timeZoneStartIndex = dateString.indexOf('+')
                if (timeZoneStartIndex > -1) {
                    val indexOfColonInTimeZone = dateString.indexOf(':', timeZoneStartIndex)
                    if (indexOfColonInTimeZone > -1) {
                        // time zone is state as '+01:00', but SimpleDateFormat expects '+0100' -> remove colon
                        dateString = dateString.removeRange(indexOfColonInTimeZone, indexOfColonInTimeZone + 1)
                    }
                }

                kickerDateFormat.parse(dateString)
            } else {
                null
            }
        } catch (e: Throwable) {
            log.error("Could not parse Kicker date", e)
            null
        }

}