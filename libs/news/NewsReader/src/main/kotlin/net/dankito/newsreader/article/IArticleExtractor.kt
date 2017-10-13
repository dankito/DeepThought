package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.model.ArticleSummaryItem


interface IArticleExtractor {

    fun getName() : String?

    fun canExtractEntryFromUrl(url: String): Boolean


    fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit)

    fun extractArticleAsync(url : String, callback: (AsyncResult<ItemExtractionResult>) -> Unit)


    fun parseHtml(extractionResult: ItemExtractionResult, html: String, url: String)

}