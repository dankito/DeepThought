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
    fun extractMultiPageArticle() {
        getAndTestArticle("https://www.heise.de/tp/features/G20-Die-Eskalation-begann-mit-der-Ernennung-von-Dudde-zum-Polizeichef-3804738.html",
                "G20: Die Eskalation begann mit der Ernennung von Dudde zum Polizeichef",
                "Das Komitee für Grundrechte und Demokratie im Nachtrag zu den Auseinandersetzungen in Hamburg",
                "https://www.heise.de/tp/imgs/89/2/2/6/0/6/1/3/Hamburgg20Polizei-f57f6ef5b9a089df.jpeg", 11700)
    }

    @Test
    fun extractVeryLargeMultiPageArticle() {
        getAndTestArticle("https://www.heise.de/tp/features/Googeln-Sie-mal-von-vorgestern-Mr-Damore-3804769.html",
                "Googeln Sie mal \"von vorgestern\", Mr. Damore",
                "Das im viel diskutierten \"Google Manifest\" formulierte Frauenbild scheint rückständig - ist jedoch aktueller denn je. Zeit für eine Debatte über einen neuen Typus \"sozial verträglicher Mann\"",
                "https://www.heise.de/tp/imgs/89/2/2/6/0/6/4/0/_Johann_Wilhelm_von_der_Pfalz_und_Anna_Maria_Luisa_de__Medici__1708_-cc6af64ad3f9af0e.jpeg", 44600)
    }

}