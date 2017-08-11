package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test
import java.text.ParseException


class TazArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return TazArticleExtractor(webClient)
    }


    @Test
    @Throws(ParseException::class)
    fun importJahr2016BrichtKlimarekordeArticle() {
        getAndTestArticle("http://taz.de/CO2-Konzentration-und-Erdtemperatur/!5439372/",
                "Das Jahr 2016 bricht Klimarekorde",
                "Die Kohlendioxid-Konzentration steigt so rasch wie nie seit Messbeginn. Auch die Erdtemperatur erreicht 2016 einen Rekordwert, berichtet die US-Klimabehörde.",
                "http://taz.de/picture/2193759/624/18915642.jpeg",
                3400, false, "CO2-Konzentration und Erdtemperatur"
                )
    }

    @Test
    @Throws(ParseException::class)
    fun importEssayUeberMassentierhaltungArticle() {
        getAndTestArticle("http://taz.de/Essay-ueber-Massentierhaltung/!5433589/",
                "Ei. Ei. Eieieieiei",
                "Der Gifteier-Skandal betrifft überwiegend große Farmen mit mehr als 15.000 Hennen. Er zeigt, wie riskant die industrielle Geflügelhaltung ist.",
                "http://taz.de/picture/2194530/624/18914385.jpeg",
                12000, false, "Essay über Massentierhaltung"
                )
    }

    @Test
    @Throws(ParseException::class)
    fun importKampfDerKackkulturenArticle() {
        getAndTestArticle("http://taz.de/Kolumne-Liebeserklaerung/!5439183/",
                "Kampf der Kackkulturen",
                "In Köln wird ein Hockklo installiert und besorgte Bürger sehen das Abendland in Fäkalien versinken. Dabei sind deutsche Sitzklos scheiße.",
                "http://taz.de/picture/2191059/624/00liebeserkla_rung_breit_2_col.jpeg",
                4100, false, "Kolumne Liebeserklärung"
        )
    }

}
