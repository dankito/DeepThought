package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class DerFreitagArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return DerFreitagArticleSummaryExtractor(webClient)
    }

}