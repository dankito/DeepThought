package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import org.junit.Test

class LeMondeDiplomatiqueArticleExtractorTest() : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return LeMondeDiplomatiqueArticleExtractor(webClient)
    }


    @Test
    fun parseJerusalemWhoseVeryOwnAndGoldenCityArticle() {
        getAndTestArticle("https://mondediplo.com/2007/02/07jerusalem",
                "Jerusalem: whose very own and golden city?",
                null, "https://mondediplo.com/IMG/jpg/JerusalemSkylineDanFlickr.jpg", 34000,
                subTitle = "The politics of urban planning")
    }

    @Test
    fun parseArticleWithPayWall() {
        getAndTestArticle("https://mondediplo.com/2017/12/05israel-africa/nohou",
                "Israel tries to mend relations with Africa",
                null, "https://mondediplo.com/IMG/jpg/israelrwanda.jpg", 6000,
                subTitle = "Security, arms and investment deals")
    }

}