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


class SpiegelMobileArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val SpiegelMobileDateTimeFormat: DateFormat = SimpleDateFormat("EEEE, dd.MM.yyyy HH:mm 'Uhr'", Locale.GERMAN)

        private val log = LoggerFactory.getLogger(SpiegelMobileArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Spiegel Mobile"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "m.spiegel.de/")
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select(".spArticleContent").first()?.let { contentElement ->
            val source = extractSource(url, contentElement)

            val content = extractContent(contentElement, url)

            extractionResult.setExtractedContent(Item(content), source)
        }
    }

    private fun extractContent(contentElement: Element, articleUrl: String): String {
        var content = contentElement.select(".article-intro").first()?.outerHtml() ?: ""

        contentElement.select(".spArticleContent").first()?.let { articleColumn ->
            cleanArticleColumn(articleColumn)

            makeLinksAbsolute(articleColumn, articleUrl)
            content += articleColumn.outerHtml()
        }

        return content
    }

    private fun cleanArticleColumn(articleColumn: Element) {
        try {
            articleColumn.select(".js-author-details-wrapper, .social-media-box-wrapper, .gujAd, .GujAdHidden, .GujAdInUse, .spHeftBox," +
                    ".spAsset, .asset-social-media," +
                    ".article-function-social-media, .article-function-box, .branded_channel_teaser, #branded_channel_teaser," +
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
            articleColumn.select(".asset-list-box").forEach { assetBox ->
                if(assetBox.select("h4").first()?.text()?.contains("anzeige", true) == true) {
                    assetBox.remove()
                }
            }
        } catch(e: Exception) { log.error("Could not cleanArticleColumn", e) }
    }

    private fun extractSource(articleUrl: String, contentElement: Element): Source? {
        var title = ""
        contentElement.select("h1").first()?.let { header1 ->
            title = header1.text().trim()
            header1.remove()
        }

        var subTitle = ""
        contentElement.select("h2").first()?.let { header2 ->
            subTitle = header2.text().trim()
            header2.remove()
        }

        var previewImageUrl: String? = null
        contentElement.select("#spArticleTopAsset img").first()?.let { previewImage ->
            previewImageUrl = previewImage.attr("src")
        }

        if(title.isNotBlank()) {
            val source = Source(subTitle, articleUrl, previewImageUrl = previewImageUrl, subTitle = subTitle)

            contentElement.select("#spShortDate").first()?.let { dateElement ->
                source.publishingDate = parseSpiegelMobileDateTimeFormat(dateElement.text())
                dateElement.remove()
            }

            return source
        }

        return null
    }

    private fun parseSpiegelMobileDateTimeFormat(dateTime: String): Date? {
        try {
            return SpiegelMobileDateTimeFormat.parse(dateTime.replace("&nbsp;", ""))
        } catch (ex: Exception) {
            log.error("Could not parse Spiegel Date Format " + dateTime, ex)
        }

        return null
    }

}