package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class HeiseNewsArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return HeiseNewsArticleExtractor(webClient)
    }


    @Test
    fun extractZwistMitBundesnetzAgenturArticle() {
        getAndTestArticle("https://www.heise.de/newsticker/meldung/Zwist-mit-Bundesnetzagentur-Keine-Neuanmeldungen-beim-E-Mail-Anbieter-FastMail-in-Deutschland-3755918.html",
                "Zwist mit Bundesnetzagentur: Keine Neuanmeldungen beim E-Mail-Anbieter FastMail in Deutschland",
                "Der altgediente australische E-Mail-Hoster hat seine Apps in Reaktion auf eine Anfrage der Bundesnetzagentur vom deutschen Markt zurückgezogen – eine Registrierung mit deutscher IP ist nicht länger möglich.",
                "https://1.f.ix.de/scale/geometry/695/q75/imgs/18/2/2/2/9/1/2/5/Bildschirmfoto_2017-06-26_um_14-d3d5e93bbef11247.png", 3000)
    }

    @Test
    fun extractMultiPageArticle() {
        getAndTestArticle("https://www.heise.de/newsticker/meldung/50-Jahre-Geldautomat-den-kann-ich-gut-gebrauchen-3750924.html",
                "50 Jahre Geldautomat – \"den kann ich gut gebrauchen\"",
                "Es ist selbstverständlich geworden, an jeder Straßenecke Bargeld aus dem Automaten ziehen zu können. Doch Geldautomaten gibt es erst seit 50 Jahren. Ihre Erfindung verdankt die Maschine einer Verspätung.",
                "https://1.f.ix.de/scale/geometry/695/q75/imgs/18/2/2/2/5/7/6/8/2160544145_a1d9d08f19_o-308b3c396dd30932.jpeg", 7000) // 6700 is approximately the length of the first page
    }

}