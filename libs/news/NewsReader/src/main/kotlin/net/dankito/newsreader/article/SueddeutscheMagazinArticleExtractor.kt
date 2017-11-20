package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
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

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "sz-magazin.sueddeutsche.de/texte/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        val referenceAndAbstract = extractReferenceAndAbstract(document, url)

        document.body().select(".content").first()?.let { contentElement ->

            contentElement.select(".maincontent").first()?.let { mainContent ->
                val entry = Item(extractContent(mainContent), referenceAndAbstract?.second ?: "")

                mainContent.select(".text-image-container img, .img-text-fullwidth-container img").first()?.let {
                    referenceAndAbstract?.first?.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
                }

                extractionResult.setExtractedContent(entry, referenceAndAbstract?.first)
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


    private fun extractReferenceAndAbstract(document: Document, siteUrl: String): Pair<Source, String?>? {
        var source: Source? = null
        var abstract: String? = null

        document.body().select("#artikelhead").first()?.let { articleHeader ->
            articleHeader.select(".vorspann").first()?.let { vorspanElement ->
                vorspanElement.select("h1").first()?.let { titleElement ->
                    source = Source(titleElement.text(), siteUrl)
                    titleElement.remove()
                    vorspanElement.select(".autor").remove()
                    abstract = convertNonBreakableSpans(vorspanElement.text())
                }
            }

            extractSubTitleAndPublishingDate(articleHeader, source)
        }

        source?.let { reference ->
            return Pair<Source, String?>(reference, abstract)
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