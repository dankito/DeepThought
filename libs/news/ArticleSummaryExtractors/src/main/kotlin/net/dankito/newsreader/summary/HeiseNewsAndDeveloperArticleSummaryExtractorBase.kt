package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.HeiseNewsAndDeveloperArticleExtractorBase
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


abstract class HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    protected abstract fun getArticleExtractorClass(url: String): Class<out HeiseNewsAndDeveloperArticleExtractorBase>


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean) : ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document))

        determineHasMore(summary, url, document)

        return summary
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        var weitereMeldungenElements = document.body().select(".itemlist-nav a") // frontpage

        if(weitereMeldungenElements.size == 0) { // starting with page 2 the 'Weitere Meldungen' link changes
            weitereMeldungenElements = document.body().select("a.seite_weiter")
        }

        summary.canLoadMoreItems = weitereMeldungenElements.size == 1
        summary.nextItemsUrl = weitereMeldungenElements.first()?.let { makeLinkAbsolute(it.attr("href"), url) }
    }

    private fun extractArticles(url: String, document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractTopArticles(url, document))
        articles.addAll(extractIndexItems(url, document)) // now Heise News only
        articles.addAll(extractHeiseDeveloperArticles(url, document)) // now Heise Developer

        return articles
    }

    private fun extractTopArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val topArticleElements = document.select("a.the_content_url")

        return topArticleElements.filterNotNull().map { parseTopArticle(it, url) }.filterNotNull()
    }

    private fun parseTopArticle(contentUrlElement: Element, url: String): ArticleSummaryItem? {
        val articleUrl = makeLinkAbsolute(contentUrlElement.attr("href"), url)
        val article = ArticleSummaryItem(articleUrl, contentUrlElement.attr("title"), getArticleExtractorClass(articleUrl))

        extractDachzeile(contentUrlElement, article)

        contentUrlElement.select(".img_clip img").first()?.let {
            article.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
        }

        contentUrlElement.select("p").first()?.let { article.summary = it.text() }

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


    private fun extractHeiseDeveloperArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val articleContainers = document.body().select("#mitte_links, aside.blogs_aktuell")
        return articleContainers.select("article a").map { parseHeiseDeveloperArticle(it, url) }.filterNotNull()
    }

    private fun parseHeiseDeveloperArticle(articleElement: Element, url: String): ArticleSummaryItem? {
        articleElement.select(".akwa-article-teaser__synopsis").first()?.text()?.let { summary ->
            val articleUrl = makeLinkAbsolute(articleElement.attr("href"), url)
            val title = articleElement.select("header")?.first()?.text() ?: ""

            var previewImageUrl = articleElement.select("figure img").first()?.attr("src")
            if(previewImageUrl != null) {
                previewImageUrl = makeLinkAbsolute(previewImageUrl, url)
            }


            return ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl), summary, previewImageUrl)
        }

        return null
    }

}