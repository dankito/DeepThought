package net.dankito.newsreader.summary

import net.dankito.newsreader.article.KickerArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.extensions.attrOrNull
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class KickerArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName() = "Kicker"

    override fun getUrl(): String = "https://www.kicker.de"

    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val items = document.select(".kick__modul__item, .kick__moreNews__item")
            .filter { it.hasClass("kick__modul__item") == false || (it.classNames().size == 1 || it.hasClass("kick__modul__item--topnews")) }
            .mapNotNull { item -> mapSummaryItem(url, item) }

        val uniqueItems = filterDuplicates(items)

        return ArticleSummary(uniqueItems)
    }

    private fun mapSummaryItem(url: String, item: Element): ArticleSummaryItem? =
        item.selectFirst("h2 > a, h3 > a, h5.kick__moreNews__headline")?.let { titleElement ->
            val articleUrl = titleElement.attrOrNull("href")
                ?: item.selectFirst("a.kick__moreNews__text")?.attrOrNull("href")
                ?: ""

            var title = getTitle(item, titleElement)

            val summary = item.selectFirst(".kick__modul__teaser")?.text() ?: ""
            val previewImageUrl = item.selectFirst("a picture img")?.attrOrNull("src")

            ArticleSummaryItem(makeLinkAbsolute(articleUrl.trim(), url), title, KickerArticleExtractor::class.java, summary, previewImageUrl)
        }

    private fun getTitle(item: Element, titleElement: Element): String {
        var title = titleElement.text()
        val subTitle = item.selectFirst("p.kick__modul__subline, p.kick__moreNews__subline")?.text()
        if (subTitle != null) {
            title = "$subTitle - $title"
        }

        val isVideo = item.selectFirst(".kick__modul__patch__ic, .kick__icon-video-ic") != null
        if (isVideo) {
            title = "Video $title"
        }
        val isKickerPlus = item.selectFirst(".kick__plus__marker:not(.kick__plus__marker--free)") != null
        if (isKickerPlus) {
            title = "Kicker+ $title"
        }

        return title
    }

    private fun filterDuplicates(items: List<ArticleSummaryItem>): List<ArticleSummaryItem> {
        val itemsByUrl = items.groupBy { it.url }

        val duplicates = itemsByUrl.filter { it.value.size > 1 }
            .mapValues { it.value.toMutableList().also { it.remove(findBestItem(it)) } }
            .values.flatten()

        return items.toMutableList().apply {
            removeAll(duplicates)
        }
    }

    private fun findBestItem(items: List<ArticleSummaryItem>): ArticleSummaryItem =
        items.firstOrNull { it.summary.isNotBlank() }
            ?: items.firstOrNull { it.previewImageUrl !=  null }
            ?: items.firstOrNull { it.title.contains(" - ") }
            ?: items.first()

}