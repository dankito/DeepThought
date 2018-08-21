package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient


class LeMondeDiplomatiqueArticleSummaryExtractor(webClient: IWebClient) : LeMondeDiplomatiqueArticleSummaryExtractorBase(webClient) {


    override fun getName(): String {
        return "Le Monde diplomatique"
    }

    override fun getUrl(): String {
        return "https://www.monde-diplomatique.fr/"
    }


}