package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SueddeutscheMagazinArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val BaseUrl = "sz-magazin.sueddeutsche.de/"

        private val SueddeutscheMagazinDateFormat: DateFormat = SimpleDateFormat("dd. MMM yyyy")
    }


    override fun getName(): String? {
        return "SZ Magazin"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, BaseUrl) && url.length > (BaseUrl.length + 10) // we cannot parse home page, but BaseUrl points to home page -> url must be longer
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("article").firstOrNull()?.let { articleElement ->
            val sourceAndSummary = extractSourceAndSummary(articleElement, url)

            articleElement.select("main > .articlemain__inner").first()?.let { mainContent ->
                val item = Item((sourceAndSummary?.second ?: "") + extractContent(mainContent))

                val source = sourceAndSummary?.first

                extractionResult.setExtractedContent(item, source)
            }
        }
    }

    private fun extractContent(mainContent: Element): String {
        mainContent.select("script, #sticky-sharingbar-container, #mehrtexte, .adbox, .ad-label, #article-authorbox, #szm-footer, #footerende").remove()

        removeWhiteSpaceAtEndOfArticle(mainContent)

        loadLazyLoadingElements(mainContent)

        return mainContent.html()
    }

    private fun removeWhiteSpaceAtEndOfArticle(mainContent: Element) {
        mainContent.select(".artikel").forEach { articleElement ->
            var index = articleElement.childNodeSize() - 1

            while (index >= 0 && isEmptyNode(articleElement, index)) {
                articleElement.childNode(index).remove()
                index--
            }
        }
    }

    private fun isEmptyNode(articleElement: Element, index: Int): Boolean {
        val node = articleElement.childNode(index)

        if(node is TextNode && convertNonBreakableSpans(node.text()).isBlank()) {
            return true
        }

        if(node is Comment) {
            return true
        }

        if(node is Element && convertNonBreakableSpans(node.text()).isBlank()) {
            return true
        }

        return false
    }


    private fun extractSourceAndSummary(articleElement: Element, siteUrl: String): Pair<Source, String?>? {
        var source: Source? = null
        var summary: String? = null

        articleElement.select("header").firstOrNull()?.let { headerElement ->
            headerElement.select(".articleheader__textsection").firstOrNull()?.let { textSectionElement ->
                textSectionElement.select("h1").first()?.let { titleElement ->
                    source = Source(titleElement.text().trim(), siteUrl)

                    textSectionElement.select(".subline, .articleheader__subline text__large--italic").firstOrNull()?.let { summaryElement ->
                        summary = "<p>" + convertNonBreakableSpans(summaryElement.text().trim()) + "</p>"
                    }
                }
            }

            headerElement.select(".articleheader__mediasection").firstOrNull()?.let { mediaSectionElement ->
                mediaSectionElement.select("figure img").firstOrNull()?.let {
                    val previewImageUrl = if (it.hasAttr("data-src")) it.attr("data-src") else it.attr("src")

                    source?.previewImageUrl = makeLinkAbsolute(previewImageUrl, siteUrl)
                }
            }

            extractSubTitleAndPublishingDate(headerElement, source)
        }

        source?.let { source ->
            return Pair<Source, String?>(source, summary)
        }

        return null
    }

    private fun extractSubTitleAndPublishingDate(articleHeader: Element, source: Source?) {
        articleHeader.select(".articleheader__metabar").first()?.let { metabarElement ->
            metabarElement.select(".metabar__item--leadtag > a")?.firstOrNull()?.let { subTitleElement ->
                source?.subTitle = subTitleElement.text().trim()
            }

            metabarElement.select(".metabar__item--date").firstOrNull()?.let { dateElement ->
                val dateString = dateElement.text().trim()
                source?.setPublishingDate(parseSueddeutscheMagazinDate(dateString), dateString)
            }

            metabarElement.select(".metabar__item--issue > a > span").firstOrNull()?.let { issueElement ->
                source?.issue = issueElement.text().trim()
            }
        }
    }

    private fun parseSueddeutscheMagazinDate(dateString: String): Date? {
        return parseDateString(dateString, SueddeutscheMagazinDateFormat)
    }

}