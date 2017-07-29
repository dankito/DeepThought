package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.IWebClient
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


    override fun urlHasHttpsPrefix(): Boolean {
        return false
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}