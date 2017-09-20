package net.dankito.deepthought.ui

import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.newsreader.model.ArticleSummary


interface IRouter {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

    fun showEntriesForReference(reference: Reference)


    fun showArticleSummaryExtractorsView()

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary? = null)

    fun showReadLaterArticlesView()


    fun showCreateEntryView()

    fun showEditEntryView(entry: Entry)

    fun showEditEntryView(article: ReadLaterArticle)

    fun showEditEntryView(extractionResult: EntryExtractionResult)


    fun showEditReferenceView(reference: Reference)

    fun showEditEntryReferenceView(forEntry: Entry, reference: Reference?, series: Series?)


    fun showEditSeriesView(series: Series)

    fun showEditReferenceSeriesView(forReference: Reference, series: Series?)


    fun returnToPreviousView()

}