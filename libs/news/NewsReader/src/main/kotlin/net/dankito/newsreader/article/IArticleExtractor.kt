package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.newsreader.model.ArticleSummaryItem


interface IArticleExtractor {

    fun getName() : String?

    fun canExtractEntryFromUrl(url: String): Boolean


    fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit)

    fun extractArticleAsync(url : String, callback: (AsyncResult<EntryExtractionResult>) -> Unit)


    fun parseHtml(extractionResult: EntryExtractionResult, html: String, url: String)

}