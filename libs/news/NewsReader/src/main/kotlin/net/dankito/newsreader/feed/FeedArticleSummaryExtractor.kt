package net.dankito.newsreader.summary

import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.extractor.AsyncResult


class FeedArticleSummaryExtractor(private val feedUrl : String, private val feedReader: IFeedReader) : IArticleSummaryExtractor {


    override fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        feedReader.readFeedAsync(feedUrl) {
            callback(it)
        }
    }

    override fun loadMoreItemsAsync(callback: (AsyncResult<ArticleSummary>) -> Unit) {
    }
}