package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient

class AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return AbendzeitungMuenchenSectionMuenchenArticleSummaryExtractor(webClient)
    }

}