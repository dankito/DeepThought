package net.dankito.newsreader.summary

import net.dankito.newsreader.article.ArticleExtractorBase
import net.dankito.newsreader.article.SueddeutscheArticleExtractor
import net.dankito.newsreader.article.SueddeutscheJetztArticleExtractor
import net.dankito.newsreader.article.SueddeutscheMagazinArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class SueddeutscheArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "SZ"
    }

    override fun getUrl(): String {
        return "https://www.sueddeutsche.de/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = if (forLoadingMoreItems == false) {
            loadArticlesFromHomePage(url, document)
        } else {
            loadArticlesForSubSections(url, document)
        }

        return ArticleSummary(articles, canLoadMoreItems = !forLoadingMoreItems, nextItemsUrl = url)
    }


    private fun loadArticlesFromHomePage(url: String, document: Document): List<ArticleSummaryItem> = mutableListOf<ArticleSummaryItem>().apply {
        addAll(extractTeasers(url, document))
        addAll(extractTileTeasers(url, document))
    }

    private fun extractTeasers(siteUrl: String, document: Document): List<ArticleSummaryItem> =
        document.body().select("a.sz-teaser")
            .mapNotNull { mapTeaserElementToArticleSummaryItem(it, siteUrl) }

    private fun mapTeaserElementToArticleSummaryItem(teaserElement: Element, siteUrl: String): ArticleSummaryItem? {
        teaserElement.selectFirst(".sz-teaser__title")?.let { titleElement ->
            val articleUrl = makeLinkAbsolute(teaserElement.attr("href"), siteUrl)
            val item = ArticleSummaryItem(articleUrl, extractTitle(teaserElement, titleElement), getArticleExtractorClass(articleUrl))

            teaserElement.selectFirst("img.sz-teaser__image--mobile, img.sz-teaser__image--desktop, img")?.let {
                item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl)
            }

            teaserElement.selectFirst(".sz-teaser__summary")?.let { summaryElement ->
                summaryElement.select(".author, .more").remove()
                item.summary = summaryElement.text()
            }

            return item
        }

        return null
    }

    private fun extractTitle(teaserElement: Element, titleElement: Element): String {
        var title = titleElement.text()

        teaserElement.selectFirst(".sz-teaser__overline-title")?.let { overlineTitle ->
            title = overlineTitle.text().trim() + " - " + title
        }

        if (teaserElement.selectFirst(".sz-teaser-label-image--video") != null) {
            title = "Video - $title"
        }

        if (isSzPlusArticle(teaserElement)) {
            title = "SZ+ $title"
        }

        return title
    }

    private fun isSzPlusArticle(teaserElement: Element): Boolean {
        return teaserElement.selectFirst(".sz-teaser__overline-label > svg")?.let { svgElement ->
            svgElement.text() == "SZ Plus"
        }
            ?: false
    }


    private fun extractTileTeasers(siteUrl: String, document: Document): List<ArticleSummaryItem> =
        document.body().selectFirst(".escapism-content")?.let { tileTeasers ->
            tileTeasers.select(".tile-teaser-content")
                .mapNotNull { mapTileTeaserToArticleSummaryItem(it, siteUrl) }
        }
            ?: emptyList()

    private fun mapTileTeaserToArticleSummaryItem(contentElement: Element, siteUrl: String): ArticleSummaryItem? {
        contentElement.selectFirst("a")?.let { titleAnchor ->
            var title = titleAnchor.selectFirst(".tile-teaser-title")?.text() ?: ""
            titleAnchor.selectFirst(".tile-teaser-overline")?.let { title = it.text() + " - " + title }

            val articleUrl = makeLinkAbsolute(titleAnchor.attr("href"), siteUrl)
            val item = ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl))

            titleAnchor.selectFirst("img")?.let { item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            contentElement.selectFirst(".tile-teaser-text")?.let { item.summary = it.text() }

            return item
        }

        return null
    }


    private fun getArticleExtractorClass(articleUrl: String): Class<out ArticleExtractorBase> {
        if(articleUrl.contains("://sz-magazin.sueddeutsche.de/")) {
            return SueddeutscheMagazinArticleExtractor::class.java
        }
        else if(articleUrl.contains("://www.jetzt.de/") || articleUrl.contains("://jetzt.sueddeutsche.de/")) {
            return SueddeutscheJetztArticleExtractor::class.java
        }

        return SueddeutscheArticleExtractor::class.java
    }


    private fun loadArticlesForSubSections(url: String, document: Document): List<ArticleSummaryItem> = mutableListOf<ArticleSummaryItem>().apply {
        addAll(loadArticlesForSubSection(url, document, "politik"))

        addAll(loadArticlesForSubSection(url, document, "wirtschaft"))

        addAll(loadArticlesForSubSection(url, document, "münchen"))

        addAll(loadArticlesForSubSection(url, document, "kultur"))

        addAll(loadArticlesForSubSection(url, document, "wissen_gesundheit"))
    }

    private fun loadArticlesForSubSection(siteUrl: String, document: Document, subSectionName: String): List<ArticleSummaryItem> =
        document.body().selectFirst("li[data-name='$subSectionName'].nav-department a")?.let { politicsNavigationElement ->
            val sectionUrl = politicsNavigationElement.attr("href")

            val subSectionDoc = requestUrl(sectionUrl)
            extractTeasers(siteUrl, subSectionDoc)
        }
            ?: emptyList()

}