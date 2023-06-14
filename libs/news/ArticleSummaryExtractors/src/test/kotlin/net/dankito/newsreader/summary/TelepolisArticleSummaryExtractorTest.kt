package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.utils.web.client.IWebClient
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat

class TelepolisArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return TelepolisArticleSummaryExtractor(webClient)
    }

    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        assertThat(summary.canLoadMoreItems, `is`(true))
        assertThat(summary.nextItemsUrl, startsWith("https://www.telepolis.de/seite-"))
    }

}