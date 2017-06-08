package net.dankito.newsreader.article

import net.dankito.newsreader.model.Article
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.data_access.network.webclient.extractor.AsyncResult


interface IArticleExtractor {

    fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<Article>) -> Unit)

    fun extractArticleAsync(url : String, callback: (AsyncResult<Article>) -> Unit)

}