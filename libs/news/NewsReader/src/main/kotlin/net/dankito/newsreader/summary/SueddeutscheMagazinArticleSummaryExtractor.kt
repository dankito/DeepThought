package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.SueddeutscheMagazinArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class SueddeutscheMagazinArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "SZ Magazin"
    }

    override fun getBaseUrl(): String {
        return "http://sz-magazin.sueddeutsche.de/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val items = document.body().select("div.text-holder").map { mapToArticleSummaryItem(it, url) }.filterNotNull()

        return ArticleSummary(items)
    }

    private fun mapToArticleSummaryItem(textHolder: Element, siteUrl: String): ArticleSummaryItem? {
        textHolder.select("h2 > a").first()?.let { heading ->
            val item = ArticleSummaryItem(makeLinkAbsolute(heading.attr("href"), siteUrl), heading.text(), SueddeutscheMagazinArticleExtractor::class.java)

            item.summary = textHolder.select("p").first()?.text() ?: ""
            textHolder.parents().select("img").first()?.attr("src")?.let { item.previewImageUrl = makeLinkAbsolute(it, siteUrl) }

            return item
        }

        return null
    }
}