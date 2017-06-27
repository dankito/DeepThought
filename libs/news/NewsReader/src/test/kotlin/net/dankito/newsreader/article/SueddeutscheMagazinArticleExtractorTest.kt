package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class SueddeutscheMagazinArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return SueddeutscheMagazinArticleExtractor(webClient)
    }


    @Test
    fun extractWoranBeziehungenWirklichScheiternArticle() {
        getAndTestArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/46147/Unterschaetzte-Trennungsgruende",
                "Woran Beziehungen wirklich scheitern",
                "Herumliegende Socken, zubehörintensive Hobbys, versaute Pointen: Zum Welttag des Verzeihens präsentiert unsere Autorin eine lange Liste unterschätzter Trennungsgründe.",
                "http://sz-magazin.sueddeutsche.de/upl/images/user/509269/thumbs_text/93472.jpg",
                3300)
    }

    @Test
    fun extractArticleWithTwoArtikelElementsInContent() {
        getAndTestArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/46105",
                "Ein Blick in die Schublade",
                "Wer von Chaos so viel versteht wie Axel Hacke, kennt den kurzen Weg von 1860 München zu Trump und Berliner Bürgermeistern.",
                "http://sz-magazin.sueddeutsche.de/upl/images/user/8059/thumbs_text_fullwidth/93287.jpg",
                3400)
    }

    @Test
    fun extractArticleRemoveNewsletter() {
        getAndTestArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/46118",
                "Ab in die Kiste",
                "Mit einer Holzkiste auf dem Rad zeigt man Liebe zur Umwelt - und Leichtgläubigkeit.",
                "http://sz-magazin.sueddeutsche.de/upl/images/user/8059/thumbs_text/93255.jpg",
                3400)
    }

    @Test
    fun extractArticleWithGallery() {
        getAndTestArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/46130",
                "Sagen Sie jetzt nichts, Pierre Richard",
                "Der legendäre Komiker im Interview ohne Worte über Gérard Depardieus Ausmaße, französischen Humor und die Zigarette danach.",
                "http://sz-magazin.sueddeutsche.de/upl/images/user/8059/thumbs_text/93413.jpg",
                2600)
    }

}