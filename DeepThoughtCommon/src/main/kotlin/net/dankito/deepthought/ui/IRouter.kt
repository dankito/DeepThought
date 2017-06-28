package net.dankito.deepthought.ui

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.EntryExtractionResult


interface IRouter {

    fun showEntriesForTag(tag: Tag, entries: List<Entry>)


    fun showArticleSummaryExtractorsView()

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig)

    fun showReadLaterArticlesView()


    fun showViewEntryView(entry: Entry)

    fun showViewEntryView(extractionResult: EntryExtractionResult)


    fun showCreateEntryView()

    fun showEditEntryView(entry: Entry)

    fun showEditEntryView(extractionResult: EntryExtractionResult)


    fun returnToPreviousView()

}