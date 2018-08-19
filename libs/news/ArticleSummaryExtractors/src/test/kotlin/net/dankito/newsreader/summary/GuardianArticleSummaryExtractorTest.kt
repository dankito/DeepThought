package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class GuardianArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return GuardianArticleSummaryExtractor(webClient)
    }


    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}