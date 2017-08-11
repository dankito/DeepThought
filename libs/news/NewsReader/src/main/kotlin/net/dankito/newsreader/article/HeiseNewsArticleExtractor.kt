package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Element


class HeiseNewsArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http") && url.contains("://www.heise.de/") && (
                url.contains("://www.heise.de/newsticker/meldung/") ||
                url.contains("://www.heise.de/ix/meldung/") ||
                url.contains("://www.heise.de/security/meldung/") ||
                url.contains("://www.heise.de/make/meldung/") ||
                url.contains("://www.heise.de/mac-and-i/meldung/") )
    }


    override fun parseArticle(header: Element, articleElement: Element, url: String, title: String) : EntryExtractionResult? {
        articleElement.select(".meldung_wrapper").first()?.let { contentElement ->
            val entry = Entry(extractContent(articleElement, url))
            contentElement.select(".meldung_anrisstext").first()?.text()?.let { entry.abstractString = it }

            val publishingDate = extractPublishingDate(header)
            val reference = Reference(url, title, publishingDate)
            reference.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

            return EntryExtractionResult(entry, reference)
        }

        return null
    }

    private fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".meldung_wrapper").first()?.children()!!.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
    }

    private fun shouldFilterElement(element: Element): Boolean {
        return element.hasClass("meldung_anrisstext") || element.hasClass("widget-werbung") || containsOnlyComment(element)
    }
}