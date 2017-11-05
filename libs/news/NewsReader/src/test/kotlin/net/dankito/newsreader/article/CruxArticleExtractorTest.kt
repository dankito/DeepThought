package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import org.junit.Test


class CruxArticleExtractorTest  : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return CruxArticleExtractor(webClient)
    }


    @Test
    fun import_ImageAndQuotationsMustGetImported() {
        getAndTestArticle("http://www.kleinezeitung.at/oesterreich/5298693/Maskottchen-bestraft_Verhuellungsverbot_In-Wien-musste-ein-Hai",
                "Verhüllungsverbot: In Wien musste ein Hai Strafe zahlen",
                null, null, 7700
        )
    }
}