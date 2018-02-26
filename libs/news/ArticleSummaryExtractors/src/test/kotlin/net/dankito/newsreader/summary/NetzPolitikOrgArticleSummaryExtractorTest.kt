package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient


class NetzPolitikOrgArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return NetzPolitikOrgArticleSummaryExtractor(webClient)
    }


    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}