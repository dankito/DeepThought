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


}