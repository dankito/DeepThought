package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class ArticleSummaryExtractorTestBase {

    enum class ArticleUrlScheme {
        HttpOnly,
        HttpsOnly,
        HttpAndHttpsMixed
    }


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

        summary?.let {
            assertThat(summary.articles.size, `is`(not(0)))

            testCanLoadMoreItems(summary)

            for (article in summary.articles) {
                testArticleSummaryItem(article)
            }
        }
    }

    private fun testArticleSummaryItem(article: ArticleSummaryItem) {
        if (getArticleUrlScheme() == ArticleUrlScheme.HttpsOnly) {
            assertThat("${article.url} is not starting with https", article.url, startsWith("https://"))
        } else if (getArticleUrlScheme() == ArticleUrlScheme.HttpOnly) {
            assertThat("${article.url} is not starting with http", article.url, startsWith("http://"))
        } else {
            assertThat(article.url, startsWith("http"))
            assertThat(article.url, containsString("://"))
        }

        assertThat(article.title.length, `is`(not(0)))

        if (areEmptyArticleSummariesAllowed() == false) {
            assertThat("Summary of article $article should not be empty", article.summary.length, `is`(not(0)))
        }
    }

    protected open fun getArticleUrlScheme() : ArticleUrlScheme {
        return ArticleUrlScheme.HttpAndHttpsMixed
    }

    protected open fun areEmptyArticleSummariesAllowed() : Boolean {
        return false
    }


    protected open fun testCanLoadMoreItems(summary: ArticleSummary) {
        assertThat(summary.canLoadMoreItems, `is`(false))
        assertThat(summary.nextItemsUrl, nullValue())
    }

}