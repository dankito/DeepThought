package net.dankito.newsreader.feed

import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.FeedArticleSummary
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RomeFeedReaderTest {

    private val underTest = RomeFeedReader(OkHttpWebClient())


    @Test
    fun readWikipediaFeaturedArticlesFeed() {
        readAndTestFeed("https://en.wikipedia.org/w/api.php?action=featuredfeed&feed=featured&feedformat=atom")
    }

    @Test
    fun readNewYorkTimesFeed() {
        readAndTestFeed("http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml")
    }

    @Test
    fun readGuardianInternalFeed() {
        readAndTestFeed("https://www.theguardian.com/international/rss")
    }

    @Test
    fun readHeiseFeed() {
        readAndTestFeed("https://www.heise.de/newsticker/heise-atom.xml")
    }

    @Test
    fun readPostillonFeed() {
        readAndTestFeed("http://www.der-postillon.com/feeds/posts/default")
    }


    private fun readAndTestFeed(feedUrl: String) {
        var feed = readFeed(feedUrl)

        testFeed(feed)
    }

    private fun readFeed(feedUrl: String): FeedArticleSummary? {
        var summary: FeedArticleSummary? = null
        val countDownLatch = CountDownLatch(1)

        underTest.readFeedAsync(feedUrl) {
            summary = it.result

            countDownLatch.countDown()
        }

        countDownLatch.await(20, TimeUnit.SECONDS)

        return summary
    }


    private fun testFeed(summary: FeedArticleSummary?) {
        assertThat(summary, notNullValue())

        summary?.let { feed ->
            assertThat(feed.title, notNullValue())
            assertThat(feed.siteUrl, notNullValue())
            assertThat(feed.publishedDate, notNullValue())
            assertThat(feed.articles.size, not(`is`(0)))

            for (item in feed.articles) {
                testArticleSummaryItem(item)
            }
        }
    }

    private fun testArticleSummaryItem(item: ArticleSummaryItem) {
        assertThat(item.title.isNullOrBlank(), `is`(false))
        assertThat(item.url.isNullOrBlank(), `is`(false))
        assertThat(item.summary.isNullOrBlank(), `is`(false))
    }

}