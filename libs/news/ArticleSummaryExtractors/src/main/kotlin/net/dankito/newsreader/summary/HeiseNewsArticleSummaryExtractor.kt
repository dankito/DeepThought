package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.HeiseDeveloperArticleExtractor
import net.dankito.newsreader.article.HeiseNewsAndDeveloperArticleExtractorBase
import net.dankito.newsreader.article.HeiseNewsArticleExtractor


class HeiseNewsArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    override fun getName(): String {
        return "Heise News"
    }

    override fun getUrl(): String {
        return "https://www.heise.de/"
    }

    override fun getArticleExtractorClass(url: String): Class<out HeiseNewsAndDeveloperArticleExtractorBase> {
        if(url.startsWith("https://www.heise.de/developer/meldung/")) {
            return HeiseDeveloperArticleExtractor::class.java
        }

        return HeiseNewsArticleExtractor::class.java
    }

}