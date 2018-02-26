package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient

class DerFreitagArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return DerFreitagArticleSummaryExtractor(webClient)
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpAndHttpsMixed
    }

}