package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class ZeitArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return ZeitArticleSummaryExtractor(webClient)
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpAndHttpsMixed
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}