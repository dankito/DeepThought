package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient

class ZeitArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return ZeitArticleSummaryExtractor(webClient)
    }


    override fun urlHasHttpsPrefix(): Boolean {
        return false
    }

}