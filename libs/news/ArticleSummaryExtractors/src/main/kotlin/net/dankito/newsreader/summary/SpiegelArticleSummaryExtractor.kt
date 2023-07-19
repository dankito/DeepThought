package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.article.SpiegelArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class SpiegelArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Spiegel"
    }

    override fun getUrl(): String {
        return "https://www.spiegel.de"
    }


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = mutableListOf<ArticleSummaryItem>().apply {
            addAll(extractTeasers(url, document))
        }

        return ArticleSummary(articles)
    }


    private fun extractTeasers(siteUrl: String, document: Document): List<ArticleSummaryItem> {
        return document.body().select("article").mapNotNull { articleElement ->
            extractItemFromArticleElement(siteUrl, articleElement)
        }
    }

    private fun extractItemFromArticleElement(siteUrl: String, articleElement: Element): ArticleSummaryItem? {
        val articleUrl = articleElement.selectFirst("a")?.attr("href")
        if (articleUrl.isNullOrBlank()) {
            return null
        }

        var title = extractTitle(articleElement)
        if (title.isNullOrBlank()) {
            return null
        }
        getSpecialArticleType(articleElement)?.let { specialType ->
            title = "$specialType $title"
        }

        val summary = articleElement.selectFirst("span.leading-loose")?.let { leadingElement ->
            leadingElement.select("[data-icon-auxiliary=\"Text\"]").remove()
            leadingElement.text().trim()
        } ?: ""
        val previewImageUrl = (articleElement.selectFirst("figure img.absolute")
            ?: articleElement.selectFirst("figure img.rounded")
                )?.attr("src")

        return ArticleSummaryItem(articleUrl, title!!, getExtractorClass(articleUrl), summary, previewImageUrl)
    }

    private fun extractTitle(articleElement: Element): String? {
        return extractTitleForTeaserWithImage(articleElement)
            ?: extractTitleForTeaserWithoutImageAndSummary(articleElement)
    }

    private fun extractTitleForTeaserWithImage(articleElement: Element): String? {
        return articleElement.selectFirst("header h2 a")?.let { headerElement ->
            var title = headerElement.attr("title")
            val kickerElement = headerElement.selectFirst("span.leading-tight")
                ?: headerElement.selectFirst("span.text-primary-base")
            if (title.isNullOrBlank() && kickerElement == null) {
                title = headerElement.text()
            }

            if (title == null) {
                return null
            }

            if (kickerElement != null) {
                val kicker = kickerElement.text().trim()
                if (kicker.isNullOrBlank() == false) {
                    title = "$kicker: $title"
                }
            }

            title
        }
    }

    private fun extractTitleForTeaserWithoutImageAndSummary(articleElement: Element): String? {
        return articleElement.selectFirst("h2 > a")?.let { headerElement ->
            val title = headerElement.attr("title")
            if (title.isNullOrBlank() == false) {
                val kicker = headerElement.selectFirst("span.text-primary-base")?.text()
                if (kicker.isNullOrBlank() == false) {
                    return "$kicker: $title"
                } else title
            } else null
        }
    }

    private fun getSpecialArticleType(articleElement: Element): String? {
        articleElement.selectFirst("[data-flag-name=\"Spplus-paid\"]")?.let { return "Spiegel+" }

        articleElement.select(".spiegeldaily").first()?.let { return "Daily" }

        articleElement.select(".bento").first()?.let { return "Bento" }

        return null
    }

    private fun getExtractorClass(articleUrl: String): Class<out IArticleExtractor>? {
        if(articleUrl.contains("://www.spiegel.de/")) {
            return SpiegelArticleExtractor::class.java
        }

        return null
    }

}