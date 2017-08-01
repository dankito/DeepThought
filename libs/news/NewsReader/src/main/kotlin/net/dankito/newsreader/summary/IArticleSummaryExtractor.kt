package net.dankito.newsreader.summary

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.newsreader.model.ArticleSummary


interface IArticleSummaryExtractor {

    fun getUrl() : String

    fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit)

    fun loadMoreItemsAsync(articleSummary: ArticleSummary, callback: (AsyncResult<ArticleSummary>) -> Unit)

}