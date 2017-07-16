package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.HeiseNewsAndDeveloperArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


abstract class HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

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
        articles.addAll(extractIndexItems(url, document))

        return articles
    }

    private fun extractTopArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val topArticleElements = document.select("a.the_content_url")

        return topArticleElements.filterNotNull().map { parseTopArticle(it, url) }.filterNotNull()
    }

    private fun parseTopArticle(contentUrlElement: Element, url: String): ArticleSummaryItem? {
        val article = ArticleSummaryItem(makeLinkAbsolute(contentUrlElement.attr("href"), url), contentUrlElement.attr("title"), HeiseNewsAndDeveloperArticleExtractor::class.java)

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
            val article = ArticleSummaryItem(makeLinkAbsolute(headerElement.attr("href") ?: "", url), headerElement.text() ?: "", HeiseNewsAndDeveloperArticleExtractor::class.java)

            item.select(".indexlist_text").first()?.let { textElement ->
                article.summary = textElement.text()

                textElement.select("img").let { article.previewImageUrl = makeLinkAbsolute(it.attr("src"), url) }
            }

            return article
        }

        return null
    }

}