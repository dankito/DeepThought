package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.*


class HeiseNewsArticleSummaryExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleSummaryExtractorBase(webClient), IArticleSummaryExtractor {

    override fun getName(): String {
        return "Heise News"
    }

    override fun getUrl(): String {
        return "https://www.heise.de/"
    }

    override fun getArticleExtractorClass(url: String): Class<out ArticleExtractorBase> {
        if(url.startsWith("https://www.heise.de/developer/meldung/")) {
            return HeiseDeveloperArticleExtractor::class.java
        }
        else if(url.startsWith("https://www.heise.de/tp/")) {
            return TelepolisArticleExtractor::class.java
        }
        else if(url.contains(".heise.de/ct/artikel/")) {
            return CtArticleExtractor::class.java
        }
        else if(url.contains("://www.techstage.de/")) {
            return TechStageArticleExtractor::class.java
        }

        return HeiseNewsArticleExtractor::class.java
    }

}