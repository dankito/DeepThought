package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import org.junit.Test

class TagesschauArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return TagesschauArticleExtractor(webClient)
    }


    @Test
    fun extractEntzogeneG20AkkreditierungenArticle() {
        getAndTestArticle("http://www.tagesschau.de/ausland/g20-journalisten-101.html",
                "Verwechslungen und Jugendsünden",
                "Sechs Wochen nach dem G20-Gipfel haben erste Journalisten Bescheid bekommen, warum ihre Akkreditierung entzogen wurde. Was beim Bundeskriminalamt an Daten über sie gespeichert war, erweist sich als toxisches Datengebräu.",
                "http://www.tagesschau.de/g20-akkreditierungen-101~_v-modPremiumHalb.jpg",
                16000, false, "Entzogene G20-Akkreditierungen")
    }

}