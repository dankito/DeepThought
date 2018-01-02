package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Abendzeitung - München"
    }

    override fun getUrl(): String {
        return "http://www.abendzeitung-muenchen.de/muenchen"
    }


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean) : ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document))

        return summary
    }

    private fun extractArticles(url: String, document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractItems(url, document))

        return articles
    }

    private fun extractItems(url: String, document: Document): Collection<ArticleSummaryItem> {
        // remove hidden items from navigation
        document.body().select(".has-level-2").remove()

        return document.body().select("div.item").map { parseItem(it, url) }.filterNotNull()
    }

    private fun parseItem(itemElement: Element, siteUrl: String): ArticleSummaryItem? {
        itemElement.select("a.data").first()?.let { dataAnchor ->
            if(dataAnchor.attr("data-paidcontent") != "true") { // filter out paid articles
                val title = dataAnchor.select(".appetizer-title").first()?.text() ?: ""
                val subTitle = dataAnchor.select(".appetizer-kicker").first()?.text() ?: ""
                val displayedTitle = (if(subTitle.isBlank()) "" else subTitle + " - ") + title
                val summary = dataAnchor.select(".appetizer-text").text() ?: ""
                val previewImageUrl = getPreviewImage(itemElement, siteUrl)

                return ArticleSummaryItem(makeLinkAbsolute(dataAnchor.attr("href"), siteUrl), displayedTitle, null, summary, previewImageUrl)
            }
        }

        return null
    }

    private fun getPreviewImage(itemElement: Element, siteUrl: String): String? {
        itemElement.select("img").first()?.let { previewImage ->
            val srcSet = previewImage.attr("data-srcset")

            if(srcSet.isNotBlank()) {
                val firstSrc = srcSet.split(',')[0]

                if(firstSrc.contains(' ')) {
                    return makeLinkAbsolute(firstSrc.substring(0, firstSrc.indexOf(' ')), siteUrl)
                }
                else {
                    return makeLinkAbsolute(firstSrc, siteUrl)
                }
            }
        }

        return null
    }

}