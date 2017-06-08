package net.dankito.newsreader.summary

import net.dankito.newsreader.article.GuardianArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.webclient.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class GuardianArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "The Guardian"
    }

    override fun getBaseUrl(): String {
        return "https://www.theguardian.com"
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

        articles.addAll(extractItems(url, document))

        return articles
    }

    private fun extractItems(url: String, document: Document): Collection<ArticleSummaryItem> {
        val postElements = document.select(".fc-item__container")

        return postElements.filterNotNull().map { parseItem(it, url) }.filterNotNull()
    }

    private fun parseItem(itemElement: Element, url: String): ArticleSummaryItem? {
        itemElement.select("a.js-headline-text").first()?.let { titleAnchor ->
            val article = ArticleSummaryItem(makeLinkAbsolute(titleAnchor.attr("href"), url), "", GuardianArticleExtractor::class.java, summary = titleAnchor.text())

            val kickerElement = itemElement.select(".fc-item__kicker")
            if(kickerElement != null && kickerElement.text().isNotBlank()) {
                article.title = kickerElement.text()
            }
            else { // item without a summary -> titleAnchor's text is the title
                article.title = article.summary
                article.summary = ""
            }

            itemElement.select(".fc-item__media-wrapper picture source, .fc-item__avatar picture source").first()?.let { article.previewImageUrl = it.attr("srcset") }

            return article
        }

        return null
    }

}