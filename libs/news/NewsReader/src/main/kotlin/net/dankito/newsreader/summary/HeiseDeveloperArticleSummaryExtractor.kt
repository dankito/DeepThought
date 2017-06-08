package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.data_access.network.webclient.IWebClient
import org.jsoup.nodes.Element


class HeiseDeveloperArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {


    override fun getName(): String {
        return "Heise Developer"
    }

    override fun getBaseUrl() : String {
        return "https://www.heise.de/developer/"
    }


    override fun extractDachzeile(contentUrlElement: Element, article: ArticleSummaryItem) {
        // don't extract .dachzeile for Heise Developer as it only contains 'Topmeldungen' or 'Topartikel'
    }

}