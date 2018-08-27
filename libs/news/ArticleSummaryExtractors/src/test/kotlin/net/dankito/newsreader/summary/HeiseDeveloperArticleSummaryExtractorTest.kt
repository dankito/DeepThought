package net.dankito.newsreader.summary

import net.dankito.utils.web.client.IWebClient
import net.dankito.newsreader.model.ArticleSummary
import org.hamcrest.CoreMatchers
import org.junit.Assert

class HeiseDeveloperArticleSummaryExtractorTest : ArticleSummaryExtractorTestBase() {

    override fun createArticleSummaryExtractor(webClient: IWebClient): IArticleSummaryExtractor {
        return HeiseDeveloperArticleSummaryExtractor(webClient)
    }


    override fun testCanLoadMoreItems(summary: ArticleSummary) {
        Assert.assertThat(summary.canLoadMoreItems, CoreMatchers.`is`(true))
        Assert.assertThat(summary.nextItemsUrl, CoreMatchers.startsWith("https://www.heise.de/developer/seite-2/"))
    }

}