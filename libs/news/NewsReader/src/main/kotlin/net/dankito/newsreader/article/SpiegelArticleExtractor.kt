package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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
        document.body().select("#content-main").first()?.let { contentElement ->
            val source = extractSource(url, contentElement)

            val content = extractContent(contentElement, url)

            extractionResult.setExtractedContent(Item(content), source)
        }
    }

    private fun extractContent(contentElement: Element, articleUrl: String): String {
        var content = contentElement.select(".article-intro").first()?.outerHtml() ?: ""

        contentElement.select("#js-article-column").first()?.let { articleColumn ->
            cleanArticleColumn(articleColumn)

            makeLinksAbsolute(articleColumn, articleUrl)
            content += articleColumn.outerHtml()
        }

        return content
    }

    private fun cleanArticleColumn(articleColumn: Element) {
        try {
            articleColumn.select(".article-function-social-media, .article-function-box, .branded_channel_teaser, #branded_channel_teaser," +
                    "br.clearfix, .adition, .teads-inread, .magazin-box-inner, .noskimwords, .nointellitxt, .noIntelliTxt, .noSmartTag, .noSmartLink, style").remove()

            articleColumn.select("div div.innen").first()?.parent()?.remove()

            articleColumn.select("p").forEach { paragraph ->
                if(paragraph.text().isBlank()) {
                    paragraph.remove()
                }
            }

            articleColumn.select(".asset-box").forEach { assetBox ->
                if(assetBox.select(".asset-title").first()?.text()?.contains("anzeige", true) == true) {
                    assetBox.remove()
                }
            }
        } catch(e: Exception) { log.error("Could not cleanArticleColumn", e) }
    }

    private fun extractSource(articleUrl: String, contentElement: Element): Source? {
        contentElement.select("h2.article-title").first()?.let { headerElement ->
            val title = headerElement.select(".headline").first()?.text() ?: ""
            val subtitle = headerElement.select(".headline-intro").first()?.text() ?: ""
            val previewImageUrl = contentElement.select("#js-article-top-wide-asset img").first()?.attr("src")

            val source = Source(title, articleUrl, previewImageUrl = previewImageUrl, subTitle = subtitle)

            contentElement.select(".article-function-date time").first()?.let { timeElement ->
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
