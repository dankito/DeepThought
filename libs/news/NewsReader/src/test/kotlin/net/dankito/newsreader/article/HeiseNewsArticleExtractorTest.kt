package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import org.junit.Test

class HeiseNewsArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return HeiseNewsArticleExtractor(webClient)
    }


    @Test
    fun extractArticleMeldungArticle() {
        getAndTestArticle("https://www.heise.de/newsticker/meldung/Zeitumstellung-Wissenschaftler-warnen-vor-ewiger-Sommerzeit-4162075.html",
                "Zeitumstellung: Wissenschaftler warnen vor ewiger \"Sommerzeit\"",
                "Die Zeitumstellung abschaffen – das wollen nicht nur viele Deutsche. Doch Forscher warnen: Eine dauerhafte \"Sommerzeit\" könne schlimme Auswirkungen haben.",
                "https://heise.cloudimg.io/width/700/q75.png-lossy-75.webp-lossy-75.foil1/_www-heise-de_/imgs/18/2/4/9/9/6/3/1/Unbenannt-1-40f434de35bf8b60.jpeg", 3000)
    }

    @Test
    fun extractMultiPageArticleMeldungArticle() {
        getAndTestArticle("https://www.heise.de/newsticker/meldung/Urheberrechtsreform-Was-hat-das-EU-Parlament-tatsaechlich-beschlossen-4165818.html",
                "Zeitumstellung: Wissenschaftler warnen vor ewiger \"Sommerzeit\"",
                "Die Zeitumstellung abschaffen – das wollen nicht nur viele Deutsche. Doch Forscher warnen: Eine dauerhafte \"Sommerzeit\" könne schlimme Auswirkungen haben.",
                "https://heise.cloudimg.io/width/700/q75.png-lossy-75.webp-lossy-75.foil1/_www-heise-de_/imgs/18/2/4/9/9/6/3/1/Unbenannt-1-40f434de35bf8b60.jpeg", 3000)
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


    @Test
    fun extractMobileArticle() {
        getAndTestArticle("https://m.heise.de/newsticker/meldung/Alexander-Gerst-Umweltzerstoerung-koennte-an-Intelligenz-zweifeln-lassen-3827472.html",
                "Alexander Gerst: Umweltzerstörung könnte an Intelligenz zweifeln lassen",
                "Astronaut Alexander Gerst soll als erster Deutscher Kommandant der internationalen Raumstation ISS werden. In Düsseldorf berichtete er davon, wie seine 166 Tage im All seine Perspektive verändert haben.",
                "https://m.f.ix.de/scale/geometry/1280/q50/imgs/18/2/2/7/5/9/6/5/urn-newsml-dpa-com-20090101-150429-99-07406_large_4_3-28dd2171e1a4fa1c.jpeg@jpg", 4300)
    }

    @Test
    fun extractVideoArticle() {
        getAndTestArticle("https://m.heise.de/newsticker/meldung/EWE-und-Uni-Jena-Salzstoecke-koennen-als-grosse-Stromspeicher-dienen-3898223.html",
                "EWE und Uni Jena: Salzstöcke können als große Stromspeicher dienen",
                "Die beiden Projektpartner haben die Technik, bei der Strom in einer solebasierten Batterie gespeichert wird, nach eigenen Angaben erfolgreich getestet.",
                "https://1.f.ix.de/scale/geometry/700/q75/imgs/18/2/3/2/2/9/4/2/ewe-0dd4e09255e81e38.jpeg", 3000)
    }

}