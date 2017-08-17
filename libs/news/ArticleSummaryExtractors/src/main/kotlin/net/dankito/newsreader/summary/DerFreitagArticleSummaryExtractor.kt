package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.DerFreitagArticleExtractor
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class DerFreitagArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Der Freitag"
    }

    override fun getUrl(): String {
        return "https://www.freitag.de"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        return ArticleSummary(extractArticles(document))
    }

    private fun extractArticles(document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractArticleCards(document))

        return articles
    }

    private fun extractArticleCards(document: Document): Collection<ArticleSummaryItem> {
        return document.body().select(".c-article-card").map { mapCardElementToArticleSummaryItem(it) }.filterNotNull()
    }

    private fun mapCardElementToArticleSummaryItem(articleCardElement: Element): ArticleSummaryItem? {
        articleCardElement.select(".c-article-card__title a").first()?.let { titleAnchor ->
            val articleUrl = titleAnchor.attr("href")
            val item = ArticleSummaryItem(articleUrl, titleAnchor.text().trim(), getExtractorClassForUrl(articleUrl))

            articleCardElement.select(".c-article-card__teaser").first()?.let { teaserElement ->
                item.summary = teaserElement.text().trim()
            }

            articleCardElement.select(".c-article-card__image img").first()?.let { previewImageElement ->
                item.previewImageUrl = previewImageElement.attr("data-src")

                if(item.previewImageUrl == null) {
                    item.previewImageUrl = previewImageElement.attr("src")
                }
            }

            return item
        }

        return null
    }

    private fun getExtractorClassForUrl(articleUrl: String): Class<out IArticleExtractor>? {
        if(articleUrl.contains(".freitag.de/")) {
            return DerFreitagArticleExtractor::class.java
        }

        return null
    }


}