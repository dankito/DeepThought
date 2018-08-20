package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient


class LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}