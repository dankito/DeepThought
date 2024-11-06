package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class KickerArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return KickerArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed() = true

}