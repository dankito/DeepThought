package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.NetzPolitikOrgArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class NetzPolitikOrgArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "netzpolitik.org"
    }

    override fun getBaseUrl(): String {
        return "https://netzpolitik.org/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = document.body().select("article").map { createSummaryItem(it, url, document) }.filterNotNull()

        return ArticleSummary(articles)
    }

    private fun createSummaryItem(articleElement: Element, url: String, document: Document): ArticleSummaryItem? {
        articleElement.select(".teaser__headline a.teaser__link").firstOrNull()?.let { teaserElement ->
            val articleUrl = teaserElement.attr("href")
            val title = teaserElement.text()

            val summary = articleElement.select(".teaser__excerpt").firstOrNull()?.text() ?: ""
            val previewImageUrl = articleElement.select("img.wp-post-image").firstOrNull()?.attr("src")

            return ArticleSummaryItem(articleUrl, title, NetzPolitikOrgArticleExtractor::class.java, summary, previewImageUrl)
        }

        return null
    }

}