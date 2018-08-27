package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor(webClient)
    }

}