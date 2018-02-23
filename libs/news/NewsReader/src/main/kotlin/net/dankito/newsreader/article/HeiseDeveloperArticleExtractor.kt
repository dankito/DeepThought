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

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.heise.de/developer/meldung/") || isHttpOrHttpsUrlFromHost(url, "m.heise.de/developer/meldung/")
    }


    override fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        articleElement.select(".article-content").first()?.let { contentElement ->
            val item = Item(extractContent(articleElement, url))

            val publishingDate = extractPublishingDate(headerElement)
            val source = Source(title, url, publishingDate)
            source.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

            extractionResult.setExtractedContent(item, source)
        }
    }

}