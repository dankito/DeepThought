package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient


class SpiegelArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return SpiegelArticleSummaryExtractor(webClient)
    }


    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}