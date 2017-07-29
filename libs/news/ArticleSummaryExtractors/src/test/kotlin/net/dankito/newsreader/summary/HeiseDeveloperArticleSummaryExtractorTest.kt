package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class HeiseDeveloperArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return HeiseDeveloperArticleSummaryExtractor(webClient)
    }

}