package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient


class HeiseDeveloperArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise Developer"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.heise.de/developer/meldung/") || isHttpOrHttpsUrlFromHost(url, "m.heise.de/developer/meldung/")
    }

}