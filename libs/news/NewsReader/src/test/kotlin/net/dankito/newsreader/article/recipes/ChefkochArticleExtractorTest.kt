package net.dankito.newsreader.article.recipes

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.article.ArticleExtractorTestBase
import net.dankito.newsreader.article.IArticleExtractor
import org.junit.Test


class ChefkochArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return ChefkochArticleExtractor(webClient)
    }


    @Test
    fun importBlattspinatLasagneRecipe() {
        getAndTestArticle("http://www.chefkoch.de/rezepte/2222841355733815/Spinat-Lasagne-mit-dreierlei-Sossen.html",
                "Spinat-Lasagne mit dreierlei Soßen",
                "simples, aber köstliches Lasagnerezept mit Blattspinat",
                null, 8300, true)
    }

}