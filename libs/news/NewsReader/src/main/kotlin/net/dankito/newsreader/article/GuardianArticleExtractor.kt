package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*


class GuardianArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "The Guardian"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.theguardian.co.uk/") || isHttpOrHttpsUrlFromHost(url, "www.theguardian.co.uk/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("#article").first()?.let { articleElement ->
            articleElement.select(".mobile-only").remove()

            articleElement.select(".content__headline").first()?.let { titleElement ->
                articleElement.select(".js-content-main-column").first()?.let { contentMainElement ->
                    extractArticle(extractionResult, url, articleElement, titleElement, contentMainElement)
                }
            }
        }
    }

    private fun extractArticle(extractionResult: ItemExtractionResult, url: String, articleElement: Element, titleElement: Element, contentMainElement: Element) {
        val item = Item(extractContent(contentMainElement, articleElement))

        // TODO: but then we don't have summary in database / index anymore -> try to add it to content
//        articleElement.select(".content__standfirst").first()?.let { item.summary = it.text() }


        val source = Source(titleElement.text(), url, extractPublishingDate(contentMainElement))

        contentMainElement.select(".media-primary").first()?.let { source.previewImageUrl = extractUrlFromFigureElement(it) }

        extractionResult.setExtractedContent(item, source)
    }

    private fun extractUrlFromFigureElement(figureElement: Element): String? {
        figureElement.select("img").first()?.let {
            if(it.hasAttr("src")) {
                return it.attr("src")
            }
        }

        figureElement.select("source").maxBy { it.attr("sizes") }?.let { return it.attr("srcset") }

        return null
    }

    private fun extractContent(bodyElement: Element, articleElement: Element): String {
        val summary = articleElement.select(".content__standfirst").first()?.html()
        return summary + bodyElement.children().filter { shouldFilterElement(it) == false }.joinToString(separator = "") {
            cleanHtml(it)

            if(it.hasClass("content__article-body") || it.attr("itemprop") == "articleBody") {
                summary + it.outerHtml()
            }
            else {
                it.outerHtml()
            }
        }
    }

    private fun cleanHtml(contentElement: Element) {
        // removes article recommendations (aside), largely drawn social media buttons (.block-share) and not working expand image button (.inline-expand-image)
        contentElement.select("aside, .block-share, .inline-expand-image").remove()
        contentElement.select(".u-responsive-ratio").attr("style", "") // style="padding-bottom: 60.00%" makes a large margin at bottom of image
    }

    private fun shouldFilterElement(element: Element) : Boolean {
        return element.hasClass("mobile-only") || element.hasClass("content__meta-container") || element.hasClass("submeta")
    }

    private fun extractPublishingDate(contentMainElement: Element): Date? {
        contentMainElement.select("time.content__dateline-lm").first()?.let {
            return Date(it.attr("data-timestamp").toLong())
        }

        return null
    }

}