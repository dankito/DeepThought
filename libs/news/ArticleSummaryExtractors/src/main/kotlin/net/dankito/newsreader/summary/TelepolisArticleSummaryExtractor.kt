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

        if(forLoadingMoreItems == false) {
            articles.addAll(extractTeaserItems(siteUrl, document))

            articles.addAll(extractMostCommentedAndMostReadArticles(siteUrl, document))
        }

        return articles
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        val weitereMeldungenElement = document.body().select("a.seite_weiter").firstOrNull()

        summary.canLoadMoreItems = weitereMeldungenElement != null
        summary.nextItemsUrl = weitereMeldungenElement?.let { makeLinkAbsolute(it.attr("href"), url) }
    }


    private fun extractTopTeaserItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> {
        // or .article?
        return document.body().select(".topteaser-container a").map { mapTopTeaserElementToArticleSummaryItem(siteUrl, it) }.filterNotNull()
    }

    private fun mapTopTeaserElementToArticleSummaryItem(siteUrl: String, topTeaserElement: Element): ArticleSummaryItem? {
        topTeaserElement.select("h2").first()?.let { titleAnchor ->
            val item = ArticleSummaryItem(makeLinkAbsolute(topTeaserElement.attr("href"), siteUrl), titleAnchor.text().trim(), getArticleExtractor())

            topTeaserElement.select("p").first()?.let { summaryElement ->
                item.summary = summaryElement.text().trim()
            }

            topTeaserElement.select("img").first()?.let { previewImageElement ->
                item.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), siteUrl)
            }

            return item
        }

        return null
    }


    private fun extractArticleItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> {
        // or .article?
        return document.body().select("article.row").map { mapArticleElementToArticleSummaryItem(siteUrl, it) }.filterNotNull()
    }

    private fun mapArticleElementToArticleSummaryItem(siteUrl: String, articleElement: Element): ArticleSummaryItem? {
        articleElement.select(".tp_title").first()?.let { titleAnchor ->
            val url = extractUrl(articleElement, siteUrl)

            val item = ArticleSummaryItem(url, titleAnchor.text().trim(), getArticleExtractor())

            articleElement.select("p").first()?.let { summaryElement ->
                item.summary = summaryElement.text().trim()
            }

            articleElement.select("figure img").first()?.let { previewImageElement ->
                item.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), siteUrl)
            }

            return item
        }

        return null
    }

    private fun extractUrl(articleElement: Element, siteUrl: String): String {
        articleElement.select("a").first()?.let { articleAnchor ->
            return makeLinkAbsolute(articleAnchor.attr("href"), siteUrl)
        }

        log.warn("Could not extract article url from ${articleElement.outerHtml()}")
        return "" // TODO: what to do in this case
    }


    private fun extractTeaserItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> {
        // or .article?
        return document.body().select(".teaser_frei .row").map { mapTeaserElementToArticleSummaryItem(siteUrl, it) }.filterNotNull()
    }

    private fun mapTeaserElementToArticleSummaryItem(siteUrl: String, teaserElement: Element): ArticleSummaryItem? {
        teaserElement.select(".tp_title").first()?.let { titleAnchor ->
            val item = ArticleSummaryItem(makeLinkAbsolute(teaserElement.parent().attr("href"), siteUrl), titleAnchor.text().trim(), getArticleExtractor())

            teaserElement.select("p").first()?.let { summaryElement ->
                item.summary = summaryElement.text().trim()
            }

            teaserElement.select("img").first()?.let { previewImageElement ->
                item.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), siteUrl)
            }

            return item
        }

        return null
    }


    private fun extractMostCommentedAndMostReadArticles(siteUrl: String, document: Document): Collection<ArticleSummaryItem> {
        return document.body().select("ul.top_beitraege_list > li").map { mapMostCommentedOrMostReadListItemToArticleSummaryItem(siteUrl, it) }.filterNotNull()
    }

    private fun mapMostCommentedOrMostReadListItemToArticleSummaryItem(siteUrl: String, listItem: Element): ArticleSummaryItem? {
        listItem.select("a").first()?.let { anchorElement ->
            return ArticleSummaryItem(makeLinkAbsolute(anchorElement.attr("href"), siteUrl), anchorElement.text().trim(), getArticleExtractor())
        }

        return null
    }


    private fun getArticleExtractor() = TelepolisArticleExtractor::class.java


}