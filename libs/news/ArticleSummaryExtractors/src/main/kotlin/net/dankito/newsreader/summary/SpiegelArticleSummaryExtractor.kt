package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient
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
        return "http://www.spiegel.de"
    }


    override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary {
        val articles = mutableListOf<ArticleSummaryItem>()

        extractTeasers(url, document, articles)
        extractResortTeasers(url, document, articles)

        return ArticleSummary(articles)
    }


    private fun extractTeasers(siteUrl: String, document: Document, articles: MutableList<ArticleSummaryItem>) {
        document.select("div.teaser").forEach {
            extractArticlesFromTeaserElement(siteUrl, it, articles)
        }
    }

    private fun extractArticlesFromTeaserElement(siteUrl: String, teaser: Element, articles: MutableList<ArticleSummaryItem>) {
        teaser.select("p.article-intro").first()?.let { intro ->
            intro.select(".more-link").remove()
            val summary = intro.text().trim()

            teaser.select(".article-title a").first()?.let { titleAnchor ->
                val articleUrl = getArticleUrlFromTitleAnchor(titleAnchor, siteUrl)
                val title = extractTitle(titleAnchor)
                val previewImageUrl = extractPreviewImageUrl(teaser, siteUrl)

                articles.add(ArticleSummaryItem(articleUrl, title, getExtractorClass(articleUrl), summary, previewImageUrl))
            }
        }

        teaser.select("ul.article-list").first()?.let { articleList ->
            extractTeaserArticleList(siteUrl, articleList, articles)
        }
    }

    private fun extractTeaserArticleList(siteUrl: String, articleList: Element, articles: MutableList<ArticleSummaryItem>) {
        articleList.select("li a").forEach { article ->
            val articleUrl = getArticleUrlFromTitleAnchor(article, siteUrl)
            articles.add(ArticleSummaryItem(articleUrl, extractTitle(article), getExtractorClass(articleUrl)))
        }
    }


    private fun extractResortTeasers(siteUrl: String, document: Document, articles: MutableList<ArticleSummaryItem>) {
        document.select(".module-box div.ressort-teaser-box-top, .module-box ul.article-list").forEach { resortTeaser ->
            if("ul" == resortTeaser.tagName()) {
                extractTeaserArticleList(siteUrl, resortTeaser, articles)
            }
            else {
                extractResortTeaserBox(siteUrl, resortTeaser, articles)
            }
        }
    }

    private fun extractResortTeaserBox(siteUrl: String, teaserBox: Element, articles: MutableList<ArticleSummaryItem>) {
        teaserBox.select(".article-title a").first()?.let { titleAnchor ->
            val articleUrl = getArticleUrlFromTitleAnchor(titleAnchor, siteUrl)
            val title = extractTitle(titleAnchor)
            val previewImageUrl = extractPreviewImageUrl(teaserBox, siteUrl)
            val summary = teaserBox.select("p").first()?.text()?.trim() ?: ""

            articles.add((ArticleSummaryItem(articleUrl, title, getExtractorClass(articleUrl), summary, previewImageUrl)))
        }
    }


    private fun getArticleUrlFromTitleAnchor(titleAnchor: Element, siteUrl: String): String {
        val articleUrl = makeLinkAbsolute(titleAnchor.attr("href"), siteUrl)
        return articleUrl
    }

    private fun extractTitle(titleAnchor: Element): String {
        var title = titleAnchor.select(".headline, .asset-headline").first()?.text()?.trim() ?: ""

        titleAnchor.select(".headline-intro, .asset-headline-intro").first()?.let {
            title = it.text().trim() + " - " + title
        }

        getSpecialArticleType(titleAnchor)?.let { articleType ->
            title = "[$articleType] " + title
        }

        return title
    }

    private fun getSpecialArticleType(titleAnchor: Element): String? {
        titleAnchor.select(".spiegelplus").first()?.let { return "SpiegelPlus" }

        titleAnchor.select(".spiegeldaily").first()?.let { return "Daily" }

        titleAnchor.select(".bento").first()?.let { return "Bento" }

        return null
    }

    private fun extractPreviewImageUrl(teaser: Element, siteUrl: String): String? {
        teaser.select(".article-image-box img").first()?.let { previewImage ->
            var source = previewImage.attr("data-original")
            if(source.isBlank()) {
                source = previewImage.attr("src")
            }

            if(source.isNotBlank()) {
                return makeLinkAbsolute(source, siteUrl)
            }
        }

        teaser.select(".image-buttons-panel img").first()?.let { videoPreviewImage ->
            return makeLinkAbsolute(videoPreviewImage.attr("src"), siteUrl)
        }

        return null
    }

    private fun getExtractorClass(articleUrl: String): Class<out IArticleExtractor>? {
        if(articleUrl.contains("://www.spiegel.de/")) {
            return SpiegelArticleExtractor::class.java
        }

        return null
    }

}