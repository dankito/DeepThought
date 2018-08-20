package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode


class SueddeutscheMagazinArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "SZ Magazin"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "sz-magazin.sueddeutsche.de/texte/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        val sourceAndSummary = extractSourceAndSummary(document, url)

        document.body().select(".content").first()?.let { contentElement ->

            contentElement.select(".maincontent").first()?.let { mainContent ->
                val item = Item((sourceAndSummary?.second ?: "") + extractContent(mainContent))

                val source = sourceAndSummary?.first
                mainContent.select(".text-image-container img, .img-text-fullwidth-container img").first()?.let {
                    source?.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
                }

                extractionResult.setExtractedContent(item, source)
            }
        }
    }

    private fun extractContent(mainContent: Element): String {
        mainContent.select("script, #sticky-sharingbar-container, #mehrtexte, .adbox, .ad-label, #article-authorbox, #szm-footer, #footerende").remove()

        removeWhiteSpaceAtEndOfArticle(mainContent)

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


    private fun extractSourceAndSummary(document: Document, siteUrl: String): Pair<Source, String?>? {
        var source: Source? = null
        var summary: String? = null

        document.body().select("#artikelhead").first()?.let { articleHeader ->
            articleHeader.select(".vorspann").first()?.let { vorspanElement ->
                vorspanElement.select("h1").first()?.let { titleElement ->
                    source = Source(titleElement.text(), siteUrl)
                    titleElement.remove()
                    vorspanElement.select(".autor").remove()
                    summary = convertNonBreakableSpans(vorspanElement.outerHtml())
                }
            }

            extractSubTitleAndPublishingDate(articleHeader, source)
        }

        source?.let { source ->
            return Pair<Source, String?>(source, summary)
        }

        return null
    }

    private fun extractSubTitleAndPublishingDate(articleHeader: Element, source: Source?): Unit? {
        return articleHeader.select(".klassifizierung").first()?.let { classificationElement ->
            classificationElement.select(".label").first()?.let { labelElement ->
                source?.subTitle = labelElement.text()

                classificationElement.select("a.heft").first()?.let {
                    source?.issue = it.text().replace("Heft", "").trim()
                }
                if (labelElement.nextElementSibling() != null) {
                    source?.issue = labelElement.nextElementSibling().text()
                }
            }
        }
    }

}