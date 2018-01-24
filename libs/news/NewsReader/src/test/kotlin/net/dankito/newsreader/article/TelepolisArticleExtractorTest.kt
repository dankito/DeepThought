package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class TelepolisArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return TelepolisArticleExtractor(webClient)
    }


    @Test
    fun extractVerfassungsgerichtDucktSichArticle() {
        getAndTestArticle("https://www.heise.de/tp/news/Verfassungsgericht-duckt-sich-erneut-bei-EZB-Anleihekaeufen-weg-3803264.html",
                "Verfassungsgericht duckt sich erneut bei EZB-Anleihekäufen weg",
                "Wieder einmal hat das höchste deutsche Gericht Bedenken, überlässt die Entscheidung aber erneut dem Europäischen Gerichtshof",
                "https://1.f.ix.de/scale/geometry/700/q75/tp/imgs/89/2/2/5/9/7/4/8/ezb-351195e0a227a9ba.jpeg", 6200)
    }

    @Test
    fun extractInterviewArticle() { // should check that div.frage and div.antwort are converted to p
        getAndTestArticle("https://www.heise.de/tp/features/Der-Unterbrecher-3947609.html",
                "Der Unterbrecher",
                "Roland Benedikter über ein Jahr Donald Trump",
                "https://heise.cloudimg.io/width/700/q75.png-lossy-75.webp-lossy-75.foil1/_www-heise-de_/tp/imgs/89/2/3/5/6/3/0/9/KALENDER-trump2-698fe82bf8384ac1.jpeg", 6200)
    }

    @Test
    fun extractMultiPageArticle() { // it seems that print version has no abstract anymore
        getAndTestArticle("https://www.heise.de/tp/features/G20-Die-Eskalation-begann-mit-der-Ernennung-von-Dudde-zum-Polizeichef-3804738.html",
                "G20: Die Eskalation begann mit der Ernennung von Dudde zum Polizeichef",
                null,"https://www.heise.de/tp/imgs/89/2/2/6/0/6/1/3/Hamburgg20Polizei-f57f6ef5b9a089df.jpeg", 10000)
    }

    @Test
    fun extractVeryLargeMultiPageArticle() { // it seems that print version has no abstract anymore
        getAndTestArticle("https://www.heise.de/tp/features/Googeln-Sie-mal-von-vorgestern-Mr-Damore-3804769.html",
                "Googeln Sie mal \"von vorgestern\", Mr. Damore",
                null,"https://www.heise.de/tp/imgs/89/2/2/6/0/6/4/0/_Johann_Wilhelm_von_der_Pfalz_und_Anna_Maria_Luisa_de__Medici__1708_-cc6af64ad3f9af0e.jpeg", 43000)
    }

}