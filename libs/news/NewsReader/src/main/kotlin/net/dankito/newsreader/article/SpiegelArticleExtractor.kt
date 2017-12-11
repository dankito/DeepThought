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

class SpiegelArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private var spiegelTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        private val log = LoggerFactory.getLogger(SpiegelArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Spiegel"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.spiegel.de/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        val contentElement = document.body().getElementById("content-main")

        val source = extractSource(url, contentElement)

        val content = extractContent(contentElement, url)

        extractionResult.setExtractedContent(Item(content), source)

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
        articleColumn.select(".article-function-social-media, .article-function-box, .branded_channel_teaser, #branded_channel_teaser," +
                ".magazin-box-inner, style").remove()

        articleColumn.select(".asset-box").forEach { assetBox ->
            if(assetBox.select(".asset-title").first()?.text()?.contains("anzeige", true) == true) {
                assetBox.remove()
            }
        }
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
            val parsedDate = spiegelTimeFormat.parse(dateTime)
            return parsedDate
        } catch (ex: Exception) {
            log.error("Could not parse Spiegel Date Format " + dateTime, ex)
        }

        return null
    }

}
