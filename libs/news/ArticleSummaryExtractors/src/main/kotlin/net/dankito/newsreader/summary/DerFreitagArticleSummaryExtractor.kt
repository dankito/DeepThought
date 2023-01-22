package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.DerFreitagArticleExtractor
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.UrlUtil
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class DerFreitagArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    companion object {
        private val urlUtil = UrlUtil()
    }


    override fun getName(): String {
        return "Der Freitag"
    }

    override fun getUrl(): String {
        return "https://www.freitag.de"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        return ArticleSummary(extractArticles(document))
    }

    private fun extractArticles(document: Document) = mutableListOf<ArticleSummaryItem>().apply {
        extractBookOfTheWeek(document)?.let { add(it) }
        addAll(extractArticleCards(document))
    }

    private fun extractArticleCards(document: Document) = document.select(".c-article-card2")
        .mapNotNull { mapCardElementToArticleSummaryItem(it) }

    private fun mapCardElementToArticleSummaryItem(card2Element: Element): ArticleSummaryItem? =
        card2Element.selectFirst(".export-article-card-url")?.let { urlElement ->
            val articleUrl = urlElement.attr("href")
            val title = urlElement.text()
            val summary = card2Element.selectFirst(".c-article-card2__text")?.let { summaryElement ->
                val categoryElement = summaryElement.selectFirst("strong")
                if (categoryElement != null) {
                    val category = categoryElement.text().trim()
                    categoryElement.remove()
                    "$category: ${summaryElement.text().trim()}"
                } else summaryElement.text().trim()
            } ?: ""
            val previewImageUrl = card2Element.selectFirst("img")?.attr("src")

            ArticleSummaryItem(articleUrl, title, getExtractorClassForUrl(articleUrl), summary, previewImageUrl)
        }

    private fun extractBookOfTheWeek(document: Document): ArticleSummaryItem? =
        document.body().selectFirst("#buch")?.let { bookElement ->
            val articleUrl = bookElement.selectFirst("a")?.attr("href")
            val author = bookElement.selectFirst(".c-advertorial-info__author")?.text()
            val bookTitle = bookElement.selectFirst(".c-advertorial-info__title")?.text()
            if (articleUrl.isNullOrBlank() == false && author.isNullOrBlank() == false && bookTitle.isNullOrBlank() == false) {
                val summary = bookElement.selectFirst(".c-advertorial-info__description")?.text()?.trim() ?: ""
                val previewImageUrl = bookElement.selectFirst("img")?.attr("href")

                ArticleSummaryItem(articleUrl, "$author - $bookTitle", getExtractorClassForUrl(articleUrl), summary, previewImageUrl)
            } else null
        }

    private fun getExtractorClassForUrl(articleUrl: String): Class<out IArticleExtractor>? {
        if(articleUrl.contains(".freitag.de/")) {
            return DerFreitagArticleExtractor::class.java
        }

        return null
    }


}