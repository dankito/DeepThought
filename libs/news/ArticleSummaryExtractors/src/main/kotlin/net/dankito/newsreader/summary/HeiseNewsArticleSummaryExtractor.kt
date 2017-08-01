package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.HeiseNewsAndDeveloperArticleExtractorBase
import net.dankito.newsreader.article.HeiseNewsArticleExtractor


class HeiseNewsArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    override fun getName(): String {
        return "Heise News"
    }

    override fun getUrl(): String {
        return "https://www.heise.de/"
    }

    override fun getArticleExtractorClass(): Class<out HeiseNewsAndDeveloperArticleExtractorBase> {
        return HeiseNewsArticleExtractor::class.java
    }

}