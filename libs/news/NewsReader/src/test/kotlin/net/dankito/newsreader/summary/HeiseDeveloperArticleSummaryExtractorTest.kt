package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.IWebClient
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

class HeiseDeveloperArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return HeiseDeveloperArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        assertThat(summary.canLoadMoreItems, `is`(false))
        assertThat(summary.nextItemsUrl, CoreMatchers.nullValue())
    }

}