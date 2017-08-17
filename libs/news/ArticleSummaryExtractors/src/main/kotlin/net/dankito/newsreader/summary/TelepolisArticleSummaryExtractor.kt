package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.TelepolisArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class TelepolisArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Telepolis"
    }

    override fun getUrl(): String {
        return "https://www.heise.de/tp/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        return ArticleSummary(extractArticles(url, document))
    }

    private fun extractArticles(siteUrl: String, document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractTopTeaserItems(siteUrl, document))

        articles.addAll(extractArticleItems(siteUrl, document))

        articles.addAll(extractTeaserItems(siteUrl, document))

        articles.addAll(extractMostCommentedAndMostReadArticles(siteUrl, document))

        return articles
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
            val item = ArticleSummaryItem(makeLinkAbsolute(articleElement.parent().attr("href"), siteUrl), titleAnchor.text().trim(), getArticleExtractor())

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