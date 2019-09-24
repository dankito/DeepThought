package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import org.junit.Test

class SueddeutscheJetztArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return SueddeutscheJetztArticleExtractor(webClient)
    }


    @Test
    fun importArbeitSchlaegtStudiumArticle() {
        getAndTestArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593621/Arbeit-schlaegt-Studium",
                "Arbeit schlägt Studium!",
                "Das Studium ist die beste Zeit des Lebens - diesen Satz hört man ziemlich oft. Dabei ist das totaler Quatsch. Richtig gut wird es erst, wenn man arbeitet.",
                null, 4300)
    }

    @Test
    fun importDerDuftDesHassesArticle() {
        getAndTestArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593210/Der-Duft-des-Hasses",
                "Der Duft des Hasses",
                "Nur ein toter Moslem ist ein guter Moslem, OfenAnAsylAus, Rottet das Gesindel aus: Eine Internetseite sammelt Kommentare, die Sympathisanten der österreichischen FPÖ ins Netz stellen. Teilweise sind diese Inhalte strafbar - eine Verfolgung ihrer Verursacher ist trotzdem schwer.",
                null, 3300)
    }

    @Test
    fun importJederVonUnsHaelt60Sklaven() {
        getAndTestArticle("http://www.jetzt.de/politik/interview-mit-einer-professorin-fuer-supply-management",
                "Jeder von uns hält 60 Sklaven",
                "Und zwar durch ganz normalen Konsum. Eine BWL-Professorin erklärt, warum.",
                null, 8100)
    }

    @Test
    fun importPlastikflaschenverbotInMontreal() {
        getAndTestArticle("http://www.jetzt.de/netzteil/plastikflaschenverbot",
                "Montreal will Wasser aus Plastikflaschen verbieten",
                "Das Leitungswasser sei gut genug, sagt der Bürgermeister.",
                null, 2300)
    }

    @Test
    fun importHeuteKoennenWirUnsereNeurosenVielBesserAusleben() {
        getAndTestArticle("http://www.jetzt.de/beziehungsunfaehig/eine-psychologin-erklaert-warum-wir-nicht-beziehungsunfaehig-sind",
                "\"Heute können wir unsere Neurosen viel besser ausleben\"",
                "Die Psychologin Stefanie Stahl über den Mythos Beziehungsunfähigkeit.",
                null, 10400)
    }

    @Test
    fun importUntenrumKolumne() {
        getAndTestArticle("https://www.jetzt.de/sex/untenrum-kolumne-vorget%C3%A4uschte-orgasmen",
                "Liebe Frauen, bitte keine Fake-Orgasmen mehr!",
                null,
                "https://caching-production.jetzt.de/attachments/cjjnzth84001i0kzkj1dzpk3m-untenrum-orgasmus-vortauschen-sde.0.24.1200.628.full.jpg",
                6000)
    }

    @Test
    fun tweetsGetDisplayed() {
        getAndTestArticle("https://www.jetzt.de/politik/greta-thunberg-trifft-donald-trump",
                "Showdown zwischen Greta Thunberg und Donald Trump",
                "Der wohl wütendste Blick aller Zeiten.",
                null,
                5500,
                subTitle = "Greta Thunberg trifft auf Donald Trump")
    }

}
