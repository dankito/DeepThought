package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.HeiseDeveloperArticleExtractor
import net.dankito.newsreader.article.HeiseNewsAndDeveloperArticleExtractorBase
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Element


class HeiseDeveloperArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {


    override fun getName(): String {
        return "Heise Developer"
    }

    override fun getUrl() : String {
        return "https://www.heise.de/developer/"
    }

    override fun getArticleExtractorClass(url: String): Class<out HeiseNewsAndDeveloperArticleExtractorBase> {
        return HeiseDeveloperArticleExtractor::class.java
    }


    override fun extractDachzeile(contentUrlElement: Element, article: ArticleSummaryItem) {
        // don't extract .dachzeile for Heise Developer as it only contains 'Topmeldungen' or 'Topartikel'
    }

}