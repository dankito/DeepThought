package net.dankito.newsreader.summary

import net.dankito.newsreader.article.ArticleExtractorBase
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


abstract class HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    protected abstract fun getArticleExtractorClass(url: String): Class<out ArticleExtractorBase>


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean) : ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document))

        determineHasMore(summary, url, document)

        return summary
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        val nextPageUrl = document.body().select("[data-component=\"PaginationPager\"]")
            .firstOrNull { it.text() == "Nächste" }
            ?.selectFirst("a")
            ?.attr("href")

        if (nextPageUrl != null) {
            summary.canLoadMoreItems = true
            summary.nextItemsUrl = makeLinkAbsolute(nextPageUrl, url)
        }
    }

    private fun extractArticles(url: String, document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractHeiseNewsArticles(url, document))
        // TODO: remove as soon as current Heise Developer homepage is also outdated
        articles.addAll(extractTopArticles(url, document))
        articles.addAll(extractIndexItems(url, document)) // now Heise News only
        articles.addAll(extractHeiseDeveloperArticles(url, document)) // now Heise Developer

        return articles
    }

    private fun extractHeiseNewsArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        return document.select("article[data-component='TeaserContainer']")
                .mapNotNull { parseHeiseNewsArticle(it, url) }
    }

    private fun parseHeiseNewsArticle(articleElement: Element, url: String): ArticleSummaryItem? {
        val title = extractTitle(articleElement)
        if (title.isNullOrBlank()) {
            return null
        }

        val relativeArticleUrl = articleElement.selectFirst("a")?.attr("href")
        if (relativeArticleUrl.isNullOrBlank()) {
            return null
        }

        val articleUrl = makeLinkAbsolute(relativeArticleUrl, url)
        if (articleUrl.contains("//www.heise-events.de/", true) || articleUrl.contains("//shop.heise.de/", true) ||
                articleUrl.contains("//spiele.heise.de/", true)) { // filter out ads for events, shop offers and games
            return null
        }

        val summary = articleElement.selectFirst("[data-component=\"TeaserSynopsis\"]")?.text()
        val previewImageUrl = getPreviewImageUrl(articleElement, url)

        val article = ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl), summary ?: "", previewImageUrl)

        checkForHeisePlusArticle(article, articleElement)

        return article
    }

    private fun extractTitle(articleElement: Element): String? {
        articleElement.selectFirst("header")?.let { header ->
            val headline = header.selectFirst("[data-component=\"TeaserHeadline\"]")?.text()
            if (headline.isNullOrBlank() == false) {
                header.selectFirst("[data-component=\"TeaserKicker\"]")?.let { kicker ->
                    return kicker.text() + " - " + headline
                }
                return headline
            }

            return header.text()
        }

        return null
    }

    private fun extractTopArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val topArticleElements = document.select("a.the_content_url")

        return topArticleElements.filterNotNull().map { parseTopArticle(it, url) }.filterNotNull()
    }

    private fun parseTopArticle(contentUrlElement: Element, url: String): ArticleSummaryItem {
        val articleUrl = makeLinkAbsolute(contentUrlElement.attr("href"), url)
        val summary = contentUrlElement.selectFirst("p")?.text() ?: ""
        val previewImageUrl = getPreviewImageUrl(contentUrlElement, url)
        val article = ArticleSummaryItem(articleUrl, contentUrlElement.attr("title"), getArticleExtractorClass(articleUrl), summary, previewImageUrl)

        extractDachzeile(contentUrlElement, article)

        return article
    }

    open protected fun extractDachzeile(contentUrlElement: Element, article: ArticleSummaryItem) {
        contentUrlElement.select(".dachzeile").first()?.let {
            if (it.text().isNullOrEmpty() == false) {
                article.title = it.text() + " - " + article.title
            }
        }
    }

    private fun extractIndexItems(url: String, document: Document): Collection<ArticleSummaryItem> {
        val indexItems = document.select(".indexlist_item")

        return indexItems.filterNotNull().map { parseIndexItem(it, url) }.filterNotNull()
    }

    private fun parseIndexItem(item: Element, url: String): ArticleSummaryItem? {
        item.select("header a").firstOrNull()?.let { headerElement ->
            val articleUrl = makeLinkAbsolute(headerElement.attr("href") ?: "", url)
            val article = ArticleSummaryItem(articleUrl, headerElement.text() ?: "", getArticleExtractorClass(articleUrl))

            item.select(".indexlist_text").first()?.let { textElement ->
                article.summary = textElement.text()

                textElement.select("img").let { article.previewImageUrl = makeLinkAbsolute(it.attr("src"), url) }
            }

            return article
        }

        return null
    }

    private fun checkForHeisePlusArticle(article: ArticleSummaryItem, articleElement: Element) {
        if (isHeisePlusArticle(articleElement)) {
            article.title = "heise+ " + article.title
        }
    }

    private fun isHeisePlusArticle(articleElement: Element): Boolean {
        return articleElement.selectFirst("footer svg[width=\"78\"][height=\"24\"][role=\"img\"]") != null
    }


    private fun extractHeiseDeveloperArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val articleContainers = document.body().select("#mitte_links, aside.blogs_aktuell")
        return articleContainers.select("article a").map { parseHeiseDeveloperArticle(it, url) }.filterNotNull()
    }

    private fun parseHeiseDeveloperArticle(articleElement: Element, url: String): ArticleSummaryItem? {
        articleElement.select(".akwa-article-teaser__synopsis").first()?.text()?.let { summary ->
            val articleUrl = makeLinkAbsolute(articleElement.attr("href"), url)
            val title = articleElement.select("header")?.first()?.text() ?: ""

            val previewImageUrl = getPreviewImageUrl(articleElement, url)


            return ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl), summary, previewImageUrl)
        }

        return null
    }

    private fun getPreviewImageUrl(teaserElement: Element, siteUrl: String): String? {
        return teaserElement.select("a-img, a-img img")
            .map { it.attr("src") }
            .firstOrNull { it.isNullOrBlank() == false && it.startsWith("data:image/svg+xml,") == false }
            ?.let { makeLinkAbsolute(it, siteUrl) }
    }

}