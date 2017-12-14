package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Element


class HeiseDeveloperArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise Developer"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.heise.de/developer/meldung/") || isHttpOrHttpsUrlFromHost(url, "m.heise.de/developer/meldung/")
    }


    override fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        articleElement.select(".article-content").first()?.let { contentElement ->
            val entry = Item(extractContent(articleElement, url))

            val publishingDate = extractPublishingDate(headerElement)
            val reference = Source(title, url, publishingDate)
            reference.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

            extractionResult.setExtractedContent(entry, reference)
        }
    }

    private fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".article-content").first()?.children()!!.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
    }

    private fun shouldFilterElement(element: Element): Boolean {
        return element.hasClass("widget-werbung")
    }

}