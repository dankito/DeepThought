package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.utils.web.client.IWebClient
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
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
        var primarySummaryArticlesCount: Int? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractSummaryAsync {
            primarySummaryArticlesCount = it.result?.articles?.size

            it.result?.let { extractSummaryResult ->
                underTest.loadMoreItemsAsync(extractSummaryResult) {
                    summary = it.result

                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await(31, TimeUnit.SECONDS)

        testSummary(summary)

        assertThat(primarySummaryArticlesCount, `is`(notNullValue()))
        assertThat(summary?.articles?.size, `is`(greaterThan(primarySummaryArticlesCount!!)))
    }

}