package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class ArticleSummaryExtractorTestBase {

    protected val underTest: IArticleSummaryExtractor

    init {
        underTest = createArticleSummaryExtractor(OkHttpWebClient())
    }

    abstract fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor


    @Test
    fun extractSummary() {
        var summary: ArticleSummary? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractSummaryAsync {
            summary = it.result

            countDownLatch.countDown()
        }

        countDownLatch.await(31, TimeUnit.SECONDS)

        testSummary(summary)
    }


    protected fun testSummary(summary: ArticleSummary?) {
        assertThat(summary, notNullValue())

        summary?.let { summary ->
            assertThat(summary.articles.size, `is`(not(0)))

            testCanLoadMoreItems(summary)

            for(article in summary.articles) {
                testArticleSummaryItem(article)
            }
        }
    }

    private fun testArticleSummaryItem(article: ArticleSummaryItem) {
        if(urlHasHttpsPrefix()) {
            assertThat(article.url, startsWith("https://"))
        }
        else {
            assertThat(article.url, startsWith("http://"))
        }

        assertThat(article.title.length, `is`(not(0)))

        if(areEmptyArticleSummariesAllowed() == false) {
            assertThat(article.summary.length, `is`(not(0)))
        }
    }

    open protected fun urlHasHttpsPrefix() : Boolean {
        return true
    }

    open protected fun areEmptyArticleSummariesAllowed() : Boolean {
        return false
    }

    abstract fun testCanLoadMoreItems(summary: ArticleSummary)

}