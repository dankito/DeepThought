package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.TelepolisArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory


class TelepolisArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    companion object {
        private val log = LoggerFactory.getLogger(TelepolisArticleSummaryExtractor::class.java)
    }


    override fun getName(): String {
        return "Telepolis"
    }

    override fun getUrl(): String {
        return "https://www.heise.de/tp/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document, forLoadingMoreItems))

        determineHasMore(summary, url, document)

        summary.removeDuplicateArticles()

        return summary
    }

    private fun extractArticles(siteUrl: String, document: Document, forLoadingMoreItems: Boolean): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractTopTeaserItems(siteUrl, document))

        articles.addAll(extractArticleItems(siteUrl, document))

        if (forLoadingMoreItems == false) {
            articles.addAll(extractTeaserItems(siteUrl, document))

            articles.addAll(extractMostCommentedAndMostReadArticles(siteUrl, document))
        }

        return articles
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        val weitereMeldungenElement = document.body().selectFirst("a.seite_weiter")

        summary.canLoadMoreItems = weitereMeldungenElement != null
        summary.nextItemsUrl = weitereMeldungenElement?.let { makeLinkAbsolute(it.attr("href"), url) }
    }


    private fun extractTopTeaserItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> =
        // or .article?
        document.body().select(".topteaser-container a")
            .mapNotNull { mapTopTeaserElementToArticleSummaryItem(siteUrl, it) }

    private fun mapTopTeaserElementToArticleSummaryItem(siteUrl: String, topTeaserElement: Element): ArticleSummaryItem? =
        topTeaserElement.selectFirst("h2")?.let { titleAnchor ->
            val summary = topTeaserElement.selectFirst("p")?.text()?.trim() ?: ""
            val previewImageUrl = getPreviewImageUrl(topTeaserElement, siteUrl)

            return ArticleSummaryItem(makeLinkAbsolute(topTeaserElement.attr("href"), siteUrl), titleAnchor.text().trim(), getArticleExtractor(), summary, previewImageUrl)
        }


    private fun extractArticleItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> =
        // or .article?
        document.body().select("article.row")
            .mapNotNull { mapArticleElementToArticleSummaryItem(siteUrl, it) }

    private fun mapArticleElementToArticleSummaryItem(siteUrl: String, articleElement: Element): ArticleSummaryItem? =
        articleElement.selectFirst(".tp_title")?.let { titleAnchor ->
            val url = extractUrl(articleElement, siteUrl)
            val summary = articleElement.selectFirst("p")?.text()?.trim() ?: ""
            val previewImageUrl = getPreviewImageUrl(articleElement, siteUrl)

            return ArticleSummaryItem(url, titleAnchor.text().trim(), getArticleExtractor(), summary, previewImageUrl)
        }

    private fun extractUrl(articleElement: Element, siteUrl: String): String {
        articleElement.selectFirst("a")?.let { articleAnchor ->
            return makeLinkAbsolute(articleAnchor.attr("href"), siteUrl)
        }

        log.warn("Could not extract article url from ${articleElement.outerHtml()}")
        return "" // TODO: what to do in this case
    }


    private fun extractTeaserItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> =
        // or .article?
        document.body().select(".teaser_frei .row")
            .mapNotNull { mapTeaserElementToArticleSummaryItem(siteUrl, it) }

    private fun mapTeaserElementToArticleSummaryItem(siteUrl: String, teaserElement: Element): ArticleSummaryItem? =
        teaserElement.selectFirst(".tp_title")?.let { titleAnchor ->
            val item = ArticleSummaryItem(makeLinkAbsolute(teaserElement.parent().attr("href"), siteUrl), titleAnchor.text().trim(), getArticleExtractor())

            teaserElement.selectFirst("p")?.let { summaryElement ->
                item.summary = summaryElement.text().trim()
            }

            teaserElement.selectFirst("img")?.let { previewImageElement ->
                item.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), siteUrl)
            }

            item
        }


    private fun extractMostCommentedAndMostReadArticles(siteUrl: String, document: Document): Collection<ArticleSummaryItem> =
        document.body().select("ul.top_beitraege_list > li")
            .mapNotNull { mapMostCommentedOrMostReadListItemToArticleSummaryItem(siteUrl, it) }

    private fun mapMostCommentedOrMostReadListItemToArticleSummaryItem(siteUrl: String, listItem: Element): ArticleSummaryItem? =
        listItem.selectFirst("a")?.let { anchorElement ->
            return ArticleSummaryItem(makeLinkAbsolute(anchorElement.attr("href"), siteUrl), anchorElement.text().trim(), getArticleExtractor())
        }

    private fun getPreviewImageUrl(teaserElement: Element, siteUrl: String): String? =
        teaserElement.select("a-img, a-img img")
            .map { it.attr("src") }
            .firstOrNull { it.isNullOrBlank() == false && it.startsWith("data:image/svg+xml,") == false }
            ?.let { makeLinkAbsolute(it, siteUrl) }


    private fun getArticleExtractor() = TelepolisArticleExtractor::class.java


}