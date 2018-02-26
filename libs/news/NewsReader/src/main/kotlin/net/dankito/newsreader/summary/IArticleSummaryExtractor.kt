package net.dankito.newsreader.summary

import net.dankito.util.AsyncResult
import net.dankito.newsreader.model.ArticleSummary


interface IArticleSummaryExtractor {

    fun getUrl() : String

    fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit)

    fun loadMoreItemsAsync(articleSummary: ArticleSummary, callback: (AsyncResult<ArticleSummary>) -> Unit)

}