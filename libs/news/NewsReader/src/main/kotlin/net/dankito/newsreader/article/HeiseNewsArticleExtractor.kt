package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient


class HeiseNewsArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Heise"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return url.startsWith("http") && ( url.contains("://www.heise.de/") || url.contains("://m.heise.de/") ) && (
                url.contains(".heise.de/newsticker/meldung/") ||
                url.contains(".heise.de/ix/meldung/") ||
                url.contains(".heise.de/security/meldung/") ||
                url.contains(".heise.de/security/artikel/") ||
                url.contains(".heise.de/make/meldung/") ||
                url.contains(".heise.de/mac-and-i/meldung/") )
    }

}