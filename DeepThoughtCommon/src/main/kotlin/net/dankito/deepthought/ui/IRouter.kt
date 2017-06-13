package net.dankito.deepthought.ui

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.EntryExtractionResult


interface IRouter {

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig)

    fun showEntryView(entry: Entry)

    fun showEntryView(extractionResult: EntryExtractionResult)


    fun returnToPreviousView()

}