package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.IWebClient
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HeiseNewsArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return HeiseNewsArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        assertThat(summary.canLoadMoreItems, `is`(true))
        assertThat(summary.nextItemsUrl, startsWith("https://www.heise.de/newsticker/seite-"))
    }



    @Test
    fun loadMoreItems() {
        var summary: ArticleSummary? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractSummaryAsync {
            underTest.loadMoreItemsAsync {
                summary = it.result

                countDownLatch.countDown()
            }
        }

        countDownLatch.await(31, TimeUnit.SECONDS)

        testSummary(summary)
    }

}