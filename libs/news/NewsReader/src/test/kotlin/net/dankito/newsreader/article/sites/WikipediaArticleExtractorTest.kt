package net.dankito.newsreader.article.sites

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.article.ArticleExtractorTestBase
import net.dankito.newsreader.article.IArticleExtractor
import org.junit.Test

class WikipediaArticleExtractorTest : ArticleExtractorTestBase() {

    override fun createArticleExtractor(webClient: IWebClient): IArticleExtractor {
        return WikipediaArticleExtractor(webClient)
    }


    @Test
    fun importBilderbergGroupArticle() {
        getAndTestArticle("https://en.wikipedia.org/wiki/Bilderberg_Group",
                "Bilderberg Group",
                "The Bilderberg Group, Bilderberg conference, Bilderberg meetings or Bilderberg Club is an annual private conference of 120 to 150 people of the European and North American political elite, experts from industry, finance, academia, and the media, established in 1954 by Prince Bernhard of the Netherlands.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Bilderberg_-_Oosterbeek.jpg/250px-Bilderberg_-_Oosterbeek.jpg", 121000, true)
    }

}