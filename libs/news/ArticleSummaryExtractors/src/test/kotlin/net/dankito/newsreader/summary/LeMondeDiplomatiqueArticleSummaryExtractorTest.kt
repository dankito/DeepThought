package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient


class LeMondeDiplomatiqueArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return LeMondeDiplomatiqueArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}