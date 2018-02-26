package net.dankito.newsreader.feed

import net.dankito.newsreader.model.FeedArticleSummary
import net.dankito.util.AsyncResult


interface IFeedReader {

    fun readFeedAsync(feedUrl: String, callback: (AsyncResult<FeedArticleSummary>) -> Unit)

}