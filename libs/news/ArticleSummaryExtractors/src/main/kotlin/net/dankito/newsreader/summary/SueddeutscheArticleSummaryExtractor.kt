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
        return "http://www.sueddeutsche.de/"
    }

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = mutableListOf<ArticleSummaryItem>()

        if(forLoadingMoreItems == false) {
            loadArticlesFromHomePage(articles, url, document)
        }
        else {
            loadArticlesForSubSections(articles, url, document)
        }

        return ArticleSummary(articles, canLoadMoreItems = !forLoadingMoreItems, nextItemsUrl = url)
    }


    private fun loadArticlesFromHomePage(articles: MutableList<ArticleSummaryItem>, url: String, document: Document) {
        extractTeasers(articles, url, document)
        extractTileTeasers(articles, url, document)
    }

    private fun extractTeasers(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document) {
        articles.addAll(
                document.body().select("a.sz-teaser")
                        .map { mapTeaserElementToArticleSummaryItem(it, siteUrl) }.filterNotNull()
        )
    }

    private fun mapTeaserElementToArticleSummaryItem(teaserElement: Element, siteUrl: String): ArticleSummaryItem? {
        teaserElement.select(".sz-teaser__title").first()?.let { titleElement ->
            val articleUrl = makeLinkAbsolute(teaserElement.attr("href"), siteUrl)
            val item = ArticleSummaryItem(articleUrl, titleElement.text(), getArticleExtractorClass(articleUrl))

            teaserElement.select("img.sz-teaser__image--mobile, img.sz-teaser__image--desktop, img").first()?.let {
                item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl)
            }

            teaserElement.select(".sz-teaser__summary").first()?.let { summaryElement ->
                summaryElement.select(".author, .more").remove()
                item.summary = summaryElement.text()
            }

            teaserElement.select(".sz-teaser__overline-title").first()?.let { overlineTitle ->
                item.title = overlineTitle.text().trim() + " - " + item.title
            }

            return item
        }

        return null
    }


    private fun extractTileTeasers(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document) {
        document.body().select(".escapism-content").first()?.let { tileTeasers ->
            articles.addAll(tileTeasers.select(".tile-teaser-content").map { mapTileTeaserToArticleSummaryItem(it, siteUrl) }.filterNotNull())
        }
    }

    private fun mapTileTeaserToArticleSummaryItem(contentElement: Element, siteUrl: String): ArticleSummaryItem? {
        contentElement.select("a").first()?.let { titleAnchor ->
            var title = titleAnchor.select(".tile-teaser-title").first()?.text() ?: ""
            titleAnchor.select(".tile-teaser-overline").first()?.let { title = it.text() + " - " + title }

            val articleUrl = makeLinkAbsolute(titleAnchor.attr("href"), siteUrl)
            val item = ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl))

            titleAnchor.select("img").first()?.let { item.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", siteUrl) }

            contentElement.select(".tile-teaser-text").first()?.let { item.summary = it.text() }

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


    private fun loadArticlesForSubSections(articles: MutableList<ArticleSummaryItem>, url: String, document: Document) {
        loadArticlesForSubSection(articles, url, document, "politik")

        loadArticlesForSubSection(articles, url, document, "wirtschaft")

        loadArticlesForSubSection(articles, url, document, "münchen")

        loadArticlesForSubSection(articles, url, document, "kultur")

        loadArticlesForSubSection(articles, url, document, "wissen_gesundheit")
    }

    private fun loadArticlesForSubSection(articles: MutableList<ArticleSummaryItem>, siteUrl: String, document: Document, subSectionName: String) {
        document.body().select("li[data-name='$subSectionName'].nav-department a").first()?.let { politicsNavigationElement ->
            val sectionUrl = politicsNavigationElement.attr("href")

            val subSectionDoc = requestUrl(sectionUrl)
            extractTeasers(articles, siteUrl, subSectionDoc)
        }
    }

}