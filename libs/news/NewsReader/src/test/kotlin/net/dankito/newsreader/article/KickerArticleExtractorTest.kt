package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import org.junit.Test

class KickerArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return KickerArticleExtractor(webClient)
    }


    @Test
    fun extractArticle() {
        getAndTestArticle("https://www.kicker.de/elefant-oder-esel-vernichtende-pressestimmen-fassen-reals-krise-zusammen-1065537/artikel",
            "\"Elefant oder Esel?\": Vernichtende Pressestimmen fassen Reals Krise zusammen",
            previewImageUrl = "https://derivates.kicker.de/image/upload/f_webp/c_crop%2Cx_0%2Cy_0%2Cw_1600%2Ch_900/w_1000%2Cq_auto%2Ch_563/v1/2024/11/06/db9621a5-8cbd-4d44-a7db-a55f15447696.jpeg",
            minContentLength = 5600,
            subTitle = "Bernabeu-Niederlage auch in der Champions League")
    }

}