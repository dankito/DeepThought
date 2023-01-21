package net.dankito.newsreader.summary

import net.dankito.newsreader.article.PostillonArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class PostillonArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Der Postillon"
    }

    override fun getUrl(): String {
        return "https://www.der-postillon.com/?m=1"
    }


    override fun parseHtmlToArticleSummary(url: String, document: Document, isForLoadingMoreItems: Boolean) : ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document, isForLoadingMoreItems))

        determineHasMore(summary, url, document)

        return summary
    }

    private fun determineHasMore(summary: ArticleSummary, url: String, document: Document) {
        document.body().selectFirst("#blog-pager a.load-more")?.let {
            val nextItemsUrl = makeLinkAbsolute(it.attr("data-load"), url)
            if (nextItemsUrl.isNullOrBlank() == false) {
                summary.canLoadMoreItems = true
                summary.nextItemsUrl = nextItemsUrl
            }
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
        return document.select("article")
            .mapNotNull { parsePostArticle(it, url) }
    }

    private fun parsePostArticle(postElement: Element, url: String): ArticleSummaryItem? {
        postElement.selectFirst(".entry-title > a")?.let { titleAnchor ->
            val summary = postElement.selectFirst(".excerpt")?.text()
                ?.replace(" +++ +++ ", " +++\n+++ ") // for Newsticker items: place each ticker on a new line
                ?: ""
            val previewImageUrl = postElement.selectFirst("[data-image]")?.attr("data-image")

            return ArticleSummaryItem(makeLinkAbsolute(titleAnchor.attr("href"), url), titleAnchor.text(), PostillonArticleExtractor::class.java, summary, previewImageUrl)
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