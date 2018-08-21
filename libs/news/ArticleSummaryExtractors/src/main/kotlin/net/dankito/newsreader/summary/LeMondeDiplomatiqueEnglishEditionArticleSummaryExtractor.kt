package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient


class LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor(webClient: IWebClient) : LeMondeDiplomatiqueArticleSummaryExtractorBase(webClient) {


    override fun getName(): String {
        return "Le Monde diplomatique English"
    }

    override fun getUrl(): String {
        return "https://mondediplo.com/"
    }


}