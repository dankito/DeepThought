package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient


class LeMondeDiplomatiqueEnglishEditionArticleSummaryExtractor(webClient: IWebClient) : LeMondeDiplomatiqueArticleSummaryExtractorBase(webClient) {


    override fun getName(): String {
        return "Le Monde diplomatique - English edition"
    }

    override fun getUrl(): String {
        return "https://mondediplo.com/"
    }


}