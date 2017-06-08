package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient


class HeiseNewsArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    override fun getName(): String {
        return "Heise News"
    }

    override fun getBaseUrl(): String {
        return "https://www.heise.de/"
    }

}