package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.ZeitArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

class ZeitArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Zeit"
    }

    override fun getUrl(): String {
        return "http://www.zeit.de"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        return ArticleSummary(extractArticles(document))
    }

    private fun extractArticles(document: Document): List<ArticleSummaryItem> {
        val articles = mutableListOf<ArticleSummaryItem>()

        articles.addAll(extractArticleElements(document))

        return articles
    }

    private fun extractArticleElements(document: Document): Collection<ArticleSummaryItem> {
        return document.body().select("article").map { mapArticleElementToArticleSummaryItem(it) }.filterNotNull()
    }

    private fun mapArticleElementToArticleSummaryItem(articleElement: Element): ArticleSummaryItem? {
        if(articleElement.className().contains("--inhouse") == false) { // --inhouse: filter out advertisements ('VERLAGSANGEBOT')
            articleElement.select("div[class~=__container]").first()?.let { articleDiv ->
                val summary = articleDiv.select("p").first()?.text()?.trim() ?: ""
                if(summary.isNotBlank() || articleElement.className()?.contains("teaser-topic-") == true) { // teaser topics don't have a summary
                    articleDiv.select("h2 a").first()?.let { headerAnchor ->
                        return extractItemFromHeaderAnchor(headerAnchor, summary, articleElement, articleDiv)
                    }
                }
            }
        }

        return null
    }

    private fun extractItemFromHeaderAnchor(headerAnchor: Element, summary: String, articleElement: Element, articleDiv: Element): ArticleSummaryItem {
        val item = ArticleSummaryItem(headerAnchor.attr("href"), headerAnchor.attr("title").trim(), ZeitArticleExtractor::class.java, summary)

        item.previewImageUrl = extractPreviewImageUrl(articleElement)
        item.publishedDate = extractPublishingDate(articleDiv)

        if(articleElement.attr("data-zplus") == "zplus-register") {
            item.title = "ZeitPlus: " + item.title
        }

        return item
    }

    private fun extractPreviewImageUrl(articleElement: Element): String? {
        articleElement.select("figure meta[itemprop=\'url\']").first()?.let { figureUrlMetaElement ->
            return figureUrlMetaElement.attr("content")
        }

        return null
    }

    private fun extractPublishingDate(articleDiv: Element): Date? {
        articleDiv.select("div time").first()?.let { timeElement ->
            return parseIsoDateTimeString(timeElement.attr("datetime"))
        }

        return null
    }


}