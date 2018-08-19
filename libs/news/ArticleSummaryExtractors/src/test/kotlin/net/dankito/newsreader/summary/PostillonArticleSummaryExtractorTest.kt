package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import org.hamcrest.CoreMatchers
import org.junit.Assert

class PostillonArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return PostillonArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(true))
        Assert.assertThat(summary.nextItemsUrl, CoreMatchers.startsWith("http://www.der-postillon.com/search?updated-max="))
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpOnly
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}