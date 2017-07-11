package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.deepthought.model.util.EntryExtractionResult


interface IArticleExtractor {

    fun getName() : String?


    fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit)

    fun extractArticleAsync(url : String, callback: (AsyncResult<EntryExtractionResult>) -> Unit)

}