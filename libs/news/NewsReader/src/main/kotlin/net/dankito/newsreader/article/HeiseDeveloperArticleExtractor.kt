package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Element


class HeiseDeveloperArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise Developer"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("https://www.heise.de/developer/meldung/") || url.startsWith("http://www.heise.de/developer/meldung/")
    }


    override fun parseArticle(headerElement: Element, articleElement: Element, url: String, title: String): EntryExtractionResult? {
        articleElement.select(".article-content").first()?.let { contentElement ->
            val entry = Entry(extractContent(articleElement, url))
            contentElement.select(".article-content__lead").first()?.text()?.let { entry.abstractString = it }

            val publishingDate = extractPublishingDate(headerElement)
            val reference = Reference(url, title, publishingDate)
            reference.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

            return EntryExtractionResult(entry, reference)
        }

        return null
    }

    private fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".article-content").first()?.children()!!.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
    }

    private fun shouldFilterElement(element: Element): Boolean {
        return element.hasClass("article-content__lead") || element.hasClass("widget-werbung")
    }

}