package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.SueddeutscheArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class SueddeutscheArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "SZ"
    }

    override fun getBaseUrl(): String {
        return "http://www.sueddeutsche.de/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = mutableListOf<ArticleSummaryItem>()

        extractTeasers(articles, url, document)
        extractTileTeasers(articles, url, document)
        extractTeaserListItems(articles, url, document)

        return ArticleSummary(articles)
    }


    private fun extractTeasers(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document) {
        document.body().select("#sitecontent").first()?.let { siteContent ->
            articles.addAll(siteContent.select(".teaser").map { mapTeaserElementToArticleSummaryItem(it, siteUrl) }.filterNotNull())
        }
    }

    private fun mapTeaserElementToArticleSummaryItem(teaserElement: Element, siteUrl: String): ArticleSummaryItem? {
        teaserElement.select(".entry-title").first()?.let { titleElement ->
            val item = ArticleSummaryItem(makeLinkAbsolute(titleElement.attr("href"), siteUrl), titleElement.text(), SueddeutscheArticleExtractor::class.java)

            titleElement.select("img").first()?.let { item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            teaserElement.select(".entry-summary").first()?.let { summaryElement ->
                summaryElement.select(".author, .more").remove()
                item.summary = summaryElement.text()
            }

            return item
        }

        return null
    }


    private fun extractTileTeasers(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document) {
        document.body().select(".escapism-content").first()?.let { tileTeasers ->
            articles.addAll(tileTeasers.select(".tile-teaser-content").map { mapTileTeaserToArticleSummaryItem(it, siteUrl) }.filterNotNull())
        }
    }

    private fun mapTileTeaserToArticleSummaryItem(contentElement: Element, siteUrl: String): ArticleSummaryItem? {
        contentElement.select("a").first()?.let { titleAnchor ->
            var title = titleAnchor.select(".tile-teaser-title").first()?.text() ?: ""
            titleAnchor.select(".tile-teaser-overline").first()?.let { title = it.text() + " - " + title }

            val item = ArticleSummaryItem(makeLinkAbsolute(titleAnchor.attr("href"), siteUrl), title, SueddeutscheArticleExtractor::class.java)

            titleAnchor.select("img").first()?.let { item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            contentElement.select(".tile-teaser-text").first()?.let { item.summary = it.text() }

            return item
        }

        return null
    }


    private fun extractTeaserListItems(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document) {
        document.body().select("#relatedcontent").first()?.let { relatedContentElement ->
            articles.addAll(relatedContentElement.select(".teaserElement").map { mapTeaserListItemToArticleSummaryItem(it, siteUrl) }.filterNotNull())
        }
    }

    private fun mapTeaserListItemToArticleSummaryItem(teaserListItemElement: Element, siteUrl: String): ArticleSummaryItem? {
        teaserListItemElement.select("a").first()?.let {
            val item = ArticleSummaryItem(makeLinkAbsolute(it.attr("href"), siteUrl), it.text(), SueddeutscheArticleExtractor::class.java)

            it.select("img").first()?.let { item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            teaserListItemElement.select(".teaserElement__text").first()?.let { teaserTextElement ->
                teaserTextElement.select(".teaserElement__author").remove()
                item.summary = teaserTextElement.text()
            }

            return item
        }

        return null
    }

}