package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class SueddeutscheArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return SueddeutscheArticleSummaryExtractor(webClient)
    }

}