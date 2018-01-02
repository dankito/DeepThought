package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor(webClient)
    }

}