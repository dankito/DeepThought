package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.EntryExtractionResult


interface IReadLaterArticleView {

    fun showArticles(extractionResultToArticlesToMap: Map<EntryExtractionResult, ReadLaterArticle>)

}