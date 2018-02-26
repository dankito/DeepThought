package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Element


class HeiseNewsArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
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
        val source = Source(title, url, extractPublishingDate(headerElement))
        articleContentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            source.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        cleanContentElement(articleContentElement)

        makeLinksAbsolute(articleContentElement, url)

        extractionResult.setExtractedContent(Item(articleContentElement.outerHtml()), source)
    }


    // old version
    private fun parseMeldungWrapperArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, contentElement: Element, url: String, title: String) {
        val item = Item(extractContent(articleElement, url))

        val publishingDate = extractPublishingDate(headerElement)
        val source = Source(title, url, publishingDate)
        source.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

        extractionResult.setExtractedContent(item, source)
    }
}