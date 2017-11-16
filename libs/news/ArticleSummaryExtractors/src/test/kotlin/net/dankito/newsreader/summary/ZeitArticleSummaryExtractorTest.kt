package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class ZeitArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return ZeitArticleSummaryExtractor(webClient)
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpOnly
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}