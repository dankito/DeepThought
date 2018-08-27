package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class DerFreitagArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return DerFreitagArticleSummaryExtractor(webClient)
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpAndHttpsMixed
    }

}