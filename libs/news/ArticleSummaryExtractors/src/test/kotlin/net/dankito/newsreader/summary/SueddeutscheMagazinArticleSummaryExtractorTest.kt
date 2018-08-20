package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient

class SueddeutscheMagazinArticleSummaryExtractorTest: ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return SueddeutscheMagazinArticleSummaryExtractor(webClient)
    }

}