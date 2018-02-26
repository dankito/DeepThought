package net.dankito.newsreader.feed

import net.dankito.util.AsyncResult
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.summary.IArticleSummaryExtractor


class FeedArticleSummaryExtractor(private val feedUrl : String, private val feedReader: IFeedReader) : IArticleSummaryExtractor {

    override fun getUrl(): String {
        return feedUrl
    }


    override fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        feedReader.readFeedAsync(feedUrl) {
            callback(it)
        }
    }

    override fun loadMoreItemsAsync(articleSummary: ArticleSummary, callback: (AsyncResult<ArticleSummary>) -> Unit) {
    }
}