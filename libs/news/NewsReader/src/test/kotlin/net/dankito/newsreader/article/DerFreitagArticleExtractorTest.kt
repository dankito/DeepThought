package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test
import java.text.ParseException


class DerFreitagArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return DerFreitagArticleExtractor(webClient)
    }


    @Test
    @Throws(ParseException::class)
    fun importKommunismusInOhioArticle() {
        getAndTestArticle("https://www.freitag.de/autoren/florian-schmid/kommunismus-in-ohio",
                "Kommunismus in Ohio",
                "Reportage Rudolf Stumberger berichtet in seinem neuen Buch vom religiös-utopischen amerikanischen Landleben",
                null, 5800, false
                )
    }

    @Test
    @Throws(ParseException::class)
    fun importBauerSuchtGeldArticle() {
        getAndTestArticle("https://www.freitag.de/autoren/der-freitag/bauer-sucht-geld",
                "Bauer sucht Geld",
                "Tierschutz Ohne bessere Haltungsbedingungen schwindet die gesellschaftliche Akzeptanz der Landwirtschaft",
                "https://www.freitag.de/autoren/der-freitag/bauer-sucht-geld/@@images/d4a5bfb0-1634-4cee-af76-0bd1ad2003e1.jpeg",
                6900, false
                )
    }

}
