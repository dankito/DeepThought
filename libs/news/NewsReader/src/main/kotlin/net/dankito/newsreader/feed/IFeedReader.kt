package net.dankito.newsreader.feed

import net.dankito.newsreader.model.FeedArticleSummary
import net.dankito.data_access.network.webclient.extractor.AsyncResult


interface IFeedReader {

    fun readFeedAsync(feedUrl: String, callback: (AsyncResult<FeedArticleSummary>) -> Unit)

}