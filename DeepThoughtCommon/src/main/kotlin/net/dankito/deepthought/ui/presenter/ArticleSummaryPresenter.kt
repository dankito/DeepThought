package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult


class ArticleSummaryPresenter(private val articleExtractors: ArticleExtractors, private val router: IRouter) {

    fun extractArticlesSummary(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.extractSummaryAsync {
            callback(it)
        }
    }

    fun loadMoreItems(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.loadMoreItemsAsync {
            callback(it)
        }
    }


    fun getAndShowArticle(item: ArticleSummaryItem, errorCallback: (Exception) -> Unit) {
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { showArticle(it) }
                asyncResult.error?.let { errorCallback(it) }
            }
        }
    }

    private fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
    }

}