package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class GuardianArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return GuardianArticleSummaryExtractor(webClient)
    }


    override fun urlHasHttpsPrefix(): Boolean {
        return true
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}