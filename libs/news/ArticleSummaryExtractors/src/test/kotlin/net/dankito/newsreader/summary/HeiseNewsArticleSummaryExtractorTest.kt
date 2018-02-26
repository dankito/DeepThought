package net.dankito.newsreader.summary

import net.dankito.util.web.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HeiseNewsArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return HeiseNewsArticleSummaryExtractor(webClient)
    }


    override fun areEmptyArticleSummariesAllowed(): Boolean {
        return true
    }

    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        assertThat(summary.canLoadMoreItems, `is`(true))
        assertThat(summary.nextItemsUrl, startsWith("https://www.heise.de/"))
        assertThat(summary.nextItemsUrl, containsString("/seite-"))
    }



    @Test
    fun loadMoreItems() {
        var summary: ArticleSummary? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractSummaryAsync {
            it.result?.let { extractSummaryResult ->
                underTest.loadMoreItemsAsync(extractSummaryResult) {
                    summary = it.result

                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await(31, TimeUnit.SECONDS)

        testSummary(summary)
    }

}