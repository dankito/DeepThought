package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SueddeutscheArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return SueddeutscheArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        if(summary.articles.size < 100) { // loaded articles from home page
            Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(true))
        }
        else { // did load more items
            Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(false))
        }

        Assert.assertThat(summary.nextItemsUrl?.contains("://www.sueddeutsche.de"), CoreMatchers.`is`(true))
    }


    override fun getArticleUrlScheme(): ArticleUrlScheme {
        return ArticleUrlScheme.HttpAndHttpsMixed
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