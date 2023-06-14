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

    companion object {
        private val RectQueryParameterRegex = Regex("rect=[0-9,]+[&amp;|amp]")
    }

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


    private fun loadArticlesFromHomePage(url: String, document: Document): List<ArticleSummaryItem> =
        extractTeasers(url, document)

    private fun extractTeasers(siteUrl: String, document: Document): List<ArticleSummaryItem> =
        document.body().select("article")
            .mapNotNull { mapArticleTeaserToArticleSummaryItem(it, siteUrl) }

    private fun mapArticleTeaserToArticleSummaryItem(articleElement: Element, siteUrl: String): ArticleSummaryItem? =
        articleElement.selectFirst("a")?.let { articleLinkElement ->
            val dataHydrationElement = articleElement.parents().firstOrNull { it.hasAttr("data-hydration-component-name") }
            if (ignoreTeaser(dataHydrationElement)) {
                return null
            }

            articleElement.selectFirst("h3")?.let { titleElement ->
                extractTitle(titleElement, dataHydrationElement)?.let { title ->
                    val articleUrl = makeLinkAbsolute(articleLinkElement.attr("href"), siteUrl)
                    val summary = articleElement.selectFirst("[data-manual='teaser-text']")?.text()?.trim() ?: ""
                    val previewImageUrl = getPreviewImageUrl(articleElement, siteUrl)
                    val item = ArticleSummaryItem(articleUrl, title, getArticleExtractorClass(articleUrl), summary, previewImageUrl)

                    articleElement.selectFirst(".sz-teaser__summary")?.let { summaryElement ->
                        summaryElement.select(".author, .more").remove()
                        item.summary = summaryElement.text()
                    }

                    item
                }
            }
        }

    private fun ignoreTeaser(dataHydrationElement: Element?): Boolean =
        // ignore Podcasts
        dataHydrationElement?.select("h2")?.any { it.text().contains("podcast", true) } == true

    private fun extractTitle(titleElement: Element, dataHydrationElement: Element?): String? =
        titleElement.selectFirst("[data-manual='teaser-title']")?.let { teaserTitle ->
            var title = teaserTitle.text()

            titleElement.selectFirst("[data-manual='teaser-overline']")?.let { overline ->
                title = "${overline.text().trim()}: $title"
            }

            val spans = titleElement.select("span")

            if (spans.any { it.text().trim() == "Video" }) {
                title = "Video - $title"
            }
            if (spans.any { it.text().trim() == "Live" }) {
                title = "Live $title"
            }

            val isSzPlusArticle = isSzPlusArticle(titleElement, dataHydrationElement)
            if (isSzPlusArticle) {
                title = "SZ+ $title"
            }

            title
        }

    private fun isSzPlusArticle(titleElement: Element, dataHydrationElement: Element?) =
        titleElement.selectFirst("svg")?.text()?.contains("SZ Plus") == true ||
                dataHydrationElement?.attr("data-hydration-component-name") == "SZPlusGroup"

    private fun getPreviewImageUrl(articleElement: Element, siteUrl: String): String? =
        articleElement.selectFirst("img[data-manual='teaser-image']")?.let { imgElement ->
            var previewImageUrl = imgElement.attr("src") // preselected source, in most cases the largest image

            // try to find a smaller image in srcSet
            val srcSet = imgElement.attr("srcSet")
            if (srcSet.isNotBlank()) {
                srcSet.split(", ")
                    .mapNotNull {
                        val urlToSize = it.split( ' ')
                        if (urlToSize.size > 1) {
                            urlToSize[1].replace("w", "").toIntOrNull() to urlToSize[0] // map to: size to url
                        } else null
                    }
                    .sortedBy { it.first } // sort by size ascending
                    .firstOrNull { it.first != null } // select smallest image
                    ?.second // select url
                    ?.let { smallestImageUrl ->
                        // remove "rect=0,0,1427,802&amp;" as otherwise always a large image gets returned
                        previewImageUrl = RectQueryParameterRegex.replace(smallestImageUrl, "")
                    }
            }

            makeLinkAbsolute(previewImageUrl, siteUrl)
        }



    private fun getArticleExtractorClass(articleUrl: String): Class<out ArticleExtractorBase> {
        if (articleUrl.contains("://sz-magazin.sueddeutsche.de/")) {
            return SueddeutscheMagazinArticleExtractor::class.java
        } else if (articleUrl.contains("://www.jetzt.de/") || articleUrl.contains("://jetzt.sueddeutsche.de/")) {
            return SueddeutscheJetztArticleExtractor::class.java
        }

        return SueddeutscheArticleExtractor::class.java
    }


    private fun loadArticlesForSubSections(url: String, document: Document): List<ArticleSummaryItem> = mutableListOf<ArticleSummaryItem>().apply {
        addAll(loadArticlesForSubSection(url, document, "politik"))

        addAll(loadArticlesForSubSection(url, document, "kultur"))

        addAll(loadArticlesForSubSection(url, document, "wirtschaft"))

        addAll(loadArticlesForSubSection(url, document, "muenchen"))

        addAll(loadArticlesForSubSection(url, document, "bayern"))

        addAll(loadArticlesForSubSection(url, document, "leben"))

        addAll(loadArticlesForSubSection(url, document, "wissen"))

        addAll(loadArticlesForSubSection(url, document, "gesundheit"))
    }

    private fun loadArticlesForSubSection(siteUrl: String, document: Document, subSectionName: String): List<ArticleSummaryItem> {
        val sectionUrl = "https://www.sueddeutsche.de/$subSectionName"

        val subSectionDoc = requestUrl(sectionUrl)

        return extractTeasers(siteUrl, subSectionDoc)
    }

}