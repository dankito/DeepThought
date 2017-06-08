package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.webclient.IWebClient
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert

class GuardianArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return GuardianArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(false))
        Assert.assertThat(summary.nextItemsUrl, nullValue())
    }


    override fun urlHasHttpsPrefix(): Boolean {
        return true
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

}