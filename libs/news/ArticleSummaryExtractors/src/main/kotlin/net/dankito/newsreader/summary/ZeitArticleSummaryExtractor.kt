package net.dankito.newsreader.summary

import net.dankito.newsreader.article.ZeitArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ZeitArticleSummaryExtractor(webClient: IWebClient) : ArticleSummaryExtractorBase(webClient) {

    override fun getName(): String {
        return "Zeit"
    }

    override fun getUrl(): String {
        return "https://www.zeit.de/"
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
        val alreadyExtractedArticles = HashMap<String, ArticleSummaryItem>()

        return document.body().select("article").map { mapArticleElementToArticleSummaryItem(it, alreadyExtractedArticles) }.filterNotNull()
    }

    private fun mapArticleElementToArticleSummaryItem(articleElement: Element, alreadyExtractedArticles: HashMap<String, ArticleSummaryItem>): ArticleSummaryItem? {
        if(articleElement.className().contains("--inhouse") == false) { // --inhouse: filter out advertisements ('VERLAGSANGEBOT')
            articleElement.select("div[class~=__container]").first()?.let { articleDiv ->
                val summary = articleDiv.select("p").first()?.text()?.trim() ?: ""
                if(summary.isNotBlank() || articleElement.className()?.contains("teaser-topic-item") == true
                        || articleElement.className()?.contains("teaser-buzzboard") == true) { // teaser topics don't have a summary
                    articleDiv.select("h3").first()?.let { headerElement ->
                        return extractItem(headerElement, summary, articleElement, articleDiv, alreadyExtractedArticles)
                    }
                }
            }
        }

        return null
    }

    private fun extractItem(headerElement: Element, summary: String, articleElement: Element, articleDiv: Element, alreadyExtractedArticles: HashMap<String, ArticleSummaryItem>): ArticleSummaryItem? {
        val header = headerElement.text().trim()
        val url = articleElement.select("a").firstOrNull()?.attr("href") ?: ""

        if(shouldAddArticle(url, summary, alreadyExtractedArticles)) {
            val item = ArticleSummaryItem(url, header, ZeitArticleExtractor::class.java, summary)

            item.previewImageUrl = articleElement.select("img").firstOrNull()?.attr("src")

            if(articleElement.attr("data-zplus").contains("zplus")) {
                item.title = "ZeitPlus: " + item.title
            }

            alreadyExtractedArticles.put(url, item)
            return item
        }

        return null
    }

    private fun shouldAddArticle(url: String, summary: String, alreadyExtractedArticles: HashMap<String, ArticleSummaryItem>): Boolean {
        if(alreadyExtractedArticles.containsKey(url) == false) {
            return true
        }

        alreadyExtractedArticles[url]?.let { alreadyExtractedArticle ->
            if(alreadyExtractedArticle.summary.isBlank() && summary.isBlank() == false) {
                return true
            }
        }

        return false
    }


}