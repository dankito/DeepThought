package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.LeMondeDiplomatiqueArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


abstract class LeMondeDiplomatiqueArticleSummaryExtractorBase(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val summary = ArticleSummary(extractArticles(url, document, forLoadingMoreItems))

        return summary
    }

    private fun extractArticles(siteUrl: String, document: Document, forLoadingMoreItems: Boolean): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractArticleItems(siteUrl, document))

        return articles
    }


    private fun extractArticleItems(siteUrl: String, document: Document): Collection<ArticleSummaryItem> {
        return document.body().select(".enune, .unedeux div.blogs, .unedeux div.picts").select("li a").map {
            mapArticleElementToArticleSummaryItem(siteUrl, it)
        }.filterNotNull()
    }

    private fun mapArticleElementToArticleSummaryItem(siteUrl: String, articleElement: Element): ArticleSummaryItem? {
        articleElement.select(".titraille h3").first()?.let { titleElement ->
            val url = makeLinkAbsolute(articleElement.attr("href"), siteUrl)
            val title = titleElement.text()

            var summary = "" // not all article previews have a summary
            articleElement.select(".intro").first()?.let { introElement ->
                introElement.select(".suite").remove()

                summary = introElement.text().trim()
            }

            var previewImageUrl: String? = null
            articleElement.select("img.spip_logos").first()?.let { previewImage ->
                previewImageUrl = makeLinkAbsolute(previewImage.attr("src"), siteUrl)
            }

            return ArticleSummaryItem(url, title, LeMondeDiplomatiqueArticleExtractor::class.java, summary, previewImageUrl)
        }

        return null
    }


}