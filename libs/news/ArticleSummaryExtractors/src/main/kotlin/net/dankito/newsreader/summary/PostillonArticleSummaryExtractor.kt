package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.PostillonArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class PostillonArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Der Postillon"
    }

    override fun getUrl(): String {
        return "http://www.der-postillon.com"
    }


    override fun parseHtmlToArticleSummary(url: String, document: Document, isForLoadingMoreItems: Boolean) : ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document, isForLoadingMoreItems))

        determineHasMore(summary, url, document)

        return summary
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        document.body().select("#Blog1_blog-pager-older-link").first()?.let {
            summary.nextItemsUrl = makeLinkAbsolute(it.attr("href"), url)
            summary.canLoadMoreItems = summary.nextItemsUrl != null
        }

    }

    private fun extractArticles(url: String, document: Document, isForLoadingMoreItems: Boolean): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractPosts(url, document))

        if(isForLoadingMoreItems == false) {
            articles.addAll(extractArchiveArticles(url, document))
        }

        return articles
    }

    private fun extractPosts(url: String, document: Document): Collection<ArticleSummaryItem> {
        val postElements = document.select(".post")

        return postElements.filterNotNull().map { parsePostArticle(it, url) }.filterNotNull()
    }

    private fun parsePostArticle(postElement: Element, url: String): ArticleSummaryItem? {
        postElement.select(".post-title > a").first()?.let { titleAnchor ->
            val article = ArticleSummaryItem(makeLinkAbsolute(titleAnchor.attr("href"), url), titleAnchor.text(), PostillonArticleExtractor::class.java)

            postElement.select(".post-body").first()?.let { contentElement ->
                contentElement.select(".more-link").remove() // remove "mehr ..."

                article.summary = contentElement.text().replace(" +++ +++ ", " +++\n+++ ") // for Newsticker items: place each ticker on a new line

                contentElement.select("img").first()?.let { article.previewImageUrl = it.attr("src") }
            }

            return article
        }

        return null
    }

    private fun extractArchiveArticles(url: String, document: Document): Collection<ArticleSummaryItem> {
        val postElements = document.select(".archiv-artikel a")

        return postElements.filterNotNull().map { parseArchiveArticle(it, url) }.filterNotNull()
    }

    private fun parseArchiveArticle(archiveArticleElement: Element, url: String): ArticleSummaryItem? {
        archiveArticleElement.select(".text-wrapper").first()?.let {
            val article = ArticleSummaryItem(makeLinkAbsolute(archiveArticleElement.attr("href"), url), it.text(), PostillonArticleExtractor::class.java)

            archiveArticleElement.select(".img-wrapper img").first()?.let { article.previewImageUrl = it.attr("src") }

            return article
        }

        return null
    }

}