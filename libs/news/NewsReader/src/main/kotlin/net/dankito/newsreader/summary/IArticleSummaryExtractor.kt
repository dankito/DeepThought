package net.dankito.newsreader.summary

import net.dankito.newsreader.model.ArticleSummary
import net.dankito.data_access.network.webclient.extractor.AsyncResult


interface IArticleSummaryExtractor {

    fun extractSummaryAsync(callback: (AsyncResult<out ArticleSummary>) -> Unit)

    fun loadMoreItemsAsync(callback: (AsyncResult<ArticleSummary>) -> Unit)

}