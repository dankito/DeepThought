package net.dankito.newsreader.article

import net.dankito.utils.credentials.UsernamePasswordCredentials
import net.dankito.utils.web.client.IWebClient
import org.junit.Ignore
import org.junit.Test


class CtArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return CtArticleExtractor(webClient)
    }


    @Test
    fun importGefahrenDerTorNutzung() {
        getAndTestArticle("http://www.heise.de/ct/ausgabe/2013-20-Gefahren-der-Tor-Nutzung-im-Alltag-2293262.html",
                "Eigen-Tor",
                "In der aktuellen Diskussion hört man häufig den Rat, für mehr Privatsphäre und Sicherheit solle man den Anonymisierungsdienst Tor nutzen. Tatsächlich ist dies jedoch ein sehr gefährlicher Tipp. Für normale Anwender erhöht er de facto das Risiko, tatsächlich überwacht und ausspioniert zu werden.",
                null, 7700, false, "Gefahren der Tor-Nutzung im Alltag"
        )
    }

    @Test
    fun importHintergruendeDesPackstationHacks() {
        getAndTestArticle("http://www.heise.de/ct/artikel/Hintergruende-des-Packstation-Hacks-3248029.html",
                "Hintergründe des Packstation-Hacks",
                "Mit gefälschter Kundenkarte und einer App konnten Angreifer DHL-Packstationen übernehmen. c't hat die Lücke nachvollzogen und zeigt, warum der Hack bis vor kurzem so leicht war.",
                null, 10800, false
        )
    }

    @Test
    fun importFragwuerdigerDatenschutzInPolizeisystemen() {
        getAndTestArticle("http://www.heise.de/ct/ausgabe/2016-13-Fragwuerdiger-Datenschutz-in-Polizeisystemen-3227333.html",
                "Außer Kontrolle Update",
                "Die Polizei speichert viele Datensätze über Verdächtige und deren Umfeld. Prüfungen in Bundesländern erbrachten verheerende Ergebnisse: Die Einhaltung von Datenschutzvorschriften wird lax gehandhabt.",
                null, 16700, false, "Fragwürdiger Datenschutz in Polizeisystemen"
        )
    }

    @Test
    fun importMobileArticle() {
        getAndTestArticle("https://m.heise.de/ct/artikel/Die-Neuerungen-von-Linux-4-14-3831941.html",
                "Die Neuerungen von Linux 4.14",
                "Ein Zerocopy-Ansatz im Netzwerkcode verspricht einen Performance-Gewinn. Den sollen auch Optimierungen am Ext4-Dateisystem erzielen. Btrfs und SquashFS lernen einen neuen und flexibleren Kompressionsalgorithmus.",
                "https://m.f.ix.de/scale/geometry/1280/q50/ct/imgs/04/2/2/7/8/7/7/7/kl-414-1c94ab97c8b56075.png@jpg",
                16700, false
        )
    }

    @Test
    fun extractSelectArticleWithoutLogin() {
        getAndTestArticle("https://www.heise.de/select/ct/2019/18/1566919516550023",
                "Verschlossen, nicht verrammelt",
                "Passwörter sind bisher der wichtigste Schutz für unsere Online-Identität. Doch die neuen Sicherheitsschlüssel der FIDO-Allianz sind in jeder Hinsicht besser und komfortabler. Ein Blick hinter die Kulissen von FIDO2 und U2F (mit ganz wenig Krypto-Kauderwelsch).",
                "https://heise.cloudimg.io/width/900/q65.png-lossy-65.webp-lossy-65.foil1/_www-heise-de_/select/ct/2019/18/1566919516550023/contentimages/image-1565609970388979.jpg",
                2000, // extracts only teaser
                subTitle = "So funktioniert der passwortlose Login mit FIDO2")
    }

    @Ignore
    @Test
    fun extractSelectArticleWithLogin() {
        // TODO: set your credentials here
        (underTest as CtArticleExtractor).login(UsernamePasswordCredentials("", ""))

        getAndTestArticle("https://www.heise.de/select/ct/2019/18/1566919516550023",
                "Verschlossen, nicht verrammelt",
                "Passwörter sind bisher der wichtigste Schutz für unsere Online-Identität. Doch die neuen Sicherheitsschlüssel der FIDO-Allianz sind in jeder Hinsicht besser und komfortabler. Ein Blick hinter die Kulissen von FIDO2 und U2F (mit ganz wenig Krypto-Kauderwelsch).",
                "https://heise.cloudimg.io/width/900/q65.png-lossy-65.webp-lossy-65.foil1/_www-heise-de_/select/ct/2019/18/1566919516550023/contentimages/image-1565609970388979.jpg",
                16000, // extracts full article
                subTitle = "So funktioniert der passwortlose Login mit FIDO2")
    }


}
