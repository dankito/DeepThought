package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient

class SueddeutscheMagazinArticleSummaryExtractorTest: ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return SueddeutscheMagazinArticleSummaryExtractor(webClient)
    }

}