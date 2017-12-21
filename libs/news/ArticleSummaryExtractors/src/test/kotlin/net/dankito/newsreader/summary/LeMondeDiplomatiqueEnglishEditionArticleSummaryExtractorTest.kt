package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient


class LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}