package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import org.hamcrest.CoreMatchers
import org.junit.Assert

class TelepolisArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return TelepolisArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(true))
        Assert.assertThat(summary.nextItemsUrl, CoreMatchers.startsWith("https://www.heise.de/tp/?seite="))
    }

}