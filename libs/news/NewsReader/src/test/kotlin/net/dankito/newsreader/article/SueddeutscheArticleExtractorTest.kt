package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test

class SueddeutscheArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return SueddeutscheArticleExtractor(webClient)
    }


    @Test
    fun extractSiegerImMarshmallowTestArticle() {
        getAndTestArticle("http://www.sueddeutsche.de/wissen/psychologie-sieger-im-marshmallow-test-1.3557488",
                "Sieger im \"Marshmallow-Test\"",
                "Kameruner Kinder zeigen mehr Beherrschung wenn es um eine versprochene Belohnung geht als Gleichaltrige aus Deutschland. Wie ist das zu interpretieren?",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3559806/940x528?v=1498468198000",
                5600)
    }

    @Test
    fun extractArticleWithImageGallery() {
        getAndTestArticle("http://www.sueddeutsche.de/leben/kommune-revolution-am-bettrand-1.3548573",
                "Revolution am Bettrand",
                "Thomas Hesterberg machte 1967 das legendäre Foto der Kommune 1, auf dem die Bewohner ihre nackten Hintern der Kamera entgegenstrecken. Tatsächlich ging es dort gar nicht so freizügig zu. Wir zeigen weitere, bislang unveröffentlichte Bilder.",
                null, 16000) // first page has a length of little more than 2900
    }

    @Test
    fun extractMultiPageArticle() {
        getAndTestArticle("http://www.sueddeutsche.de/medien/im-visier-der-nachbarn-al-jazeera-gefuerchtete-stimme-der-massen-1.3558089",
                "Al Jazeera - gefürchtete Stimme der Massen",
                "Mit seiner professionellen Machart erreicht der TV-Sender weltweit Millionen Zuschauer. Kritiker werfen ihm vor, den Terrorismus zu fördern. Saudi-Arabien will Katar nun zwingen, den Sender zu schließen.",
                "http://media-cdn.sueddeutsche.de/image/sz.1.3558716/940x528?v=1498379214000",
                3000) // first page has a length of little more than 2900
    }


}