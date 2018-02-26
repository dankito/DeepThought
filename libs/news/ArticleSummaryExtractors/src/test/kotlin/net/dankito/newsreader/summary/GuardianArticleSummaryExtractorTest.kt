package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient

class GuardianArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return GuardianArticleSummaryExtractor(webClient)
    }


    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}