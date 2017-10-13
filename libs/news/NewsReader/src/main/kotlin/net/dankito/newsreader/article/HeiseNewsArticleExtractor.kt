package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Element


class HeiseNewsArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http") && ( url.contains("://www.heise.de/") || url.contains("://m.heise.de/") ) && (
                url.contains(".heise.de/newsticker/meldung/") ||
                url.contains(".heise.de/ix/meldung/") ||
                url.contains(".heise.de/security/meldung/") ||
                url.contains(".heise.de/security/artikel/") ||
                url.contains(".heise.de/make/meldung/") ||
                url.contains(".heise.de/mac-and-i/meldung/") )
    }


    override fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        articleElement.select(".meldung_wrapper").first()?.let { contentElement ->
            parseMeldungWrapperArticle(extractionResult, headerElement, articleElement, contentElement, url, title)
            return
        }

        articleElement.select(".article-content").first()?.let { articleContentElement ->
            parseArticleContentArticle(extractionResult, headerElement, articleContentElement, url, title)
        }
    }


    // new version
    private fun parseArticleContentArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleContentElement: Element, url: String, title: String) {
        val reference = Source(url, title, extractPublishingDate(headerElement))
        articleContentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            reference.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        val abstract = articleContentElement.select(".article-content__lead").first()?.text() ?: ""

        articleContentElement.select(".article-content__lead").remove()

        extractionResult.setExtractedContent(Item(articleContentElement.outerHtml(), abstract), reference)
    }


    // old version
    private fun parseMeldungWrapperArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, contentElement: Element, url: String, title: String) {
        val entry = Item(extractContent(articleElement, url))
        contentElement.select(".meldung_anrisstext").first()?.text()?.let { entry.summary = it }

        val publishingDate = extractPublishingDate(headerElement)
        val reference = Source(url, title, publishingDate)
        reference.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

        extractionResult.setExtractedContent(entry, reference)
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