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

    @Test
    @Throws(ParseException::class)
    fun importDasKapitalHatSchonGewaeltArticle() {
        getAndTestArticle("https://www.freitag.de/autoren/felix-werdermann/das-kapital-gibt-am-liebsten-den-kindern",
                "Das Kapital hat schon gewählt",
                "Parteienfinanzierung Gegen eine mögliche Einflussnahme der Privatwirtschaft auf die Politik würde vor allem eines helfen: Ein Verbot von Unternehmensspenden",
                "https://www.freitag.de/autoren/felix-werdermann/das-kapital-gibt-am-liebsten-den-kindern/@@images/9a3d1f14-c861-4e36-99e2-4f00fdcac336.jpeg",
                5000, false
        )
    }

    @Test
    @Throws(ParseException::class)
    fun importDerChefKassiertZweiMillionenArticle() {
        getAndTestArticle("https://www.freitag.de/autoren/der-freitag/der-chef-kassiert-zwei-millionen",
                "Der Chef kassiert zwei Millionen",
                "Daseinsvorsorge Gewinne sprudeln, Mieter gehen auf die Barrikaden: ein Porträt des Immobilienkonzerns Deutsche Wohnen",
                "https://www.freitag.de/autoren/der-freitag/der-chef-kassiert-zwei-millionen/@@images/a4b008f7-3b83-4f28-b238-9d487070c6a0.jpeg",
                7700, false
        )
    }

    @Test
    @Throws(ParseException::class)
    fun ensureQuestionsGetRemoved() {
        getAndTestArticle("https://www.freitag.de/autoren/der-freitag/keine-nebensaechlichkeiten-des-wahlkampfes",
                "Keine \"Nebensächlichkeiten des Wahlkampfes\"",
                "Gastbeitrag Will die SPD wieder linke Volkspartei werden, braucht sie emanzipatorische Alternativen zum Kapitalismus, schreibt Dierk Hirschel, Mitglied der SPD-Grundwertekommission",
                "https://www.freitag.de/autoren/der-freitag/keine-nebensaechlichkeiten-des-wahlkampfes/@@images/5c672e04-42c9-477f-9a9f-0ab2411baa2a.jpeg",
                22800, false
        )
    }

}
