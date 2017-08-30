package net.dankito.deepthought.ui

import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult


interface IRouter {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

    fun showEntriesForReference(reference: Reference)


    fun showArticleSummaryExtractorsView()

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig)

    fun showReadLaterArticlesView()


    fun showCreateEntryView()

    fun showEditEntryView(entry: Entry, field: EntryField? = null)

    fun showEditEntryView(article: ReadLaterArticle, field: EntryField? = null)

    fun showEditEntryView(extractionResult: EntryExtractionResult, field: EntryField? = null)


    fun showCreateReferenceView()

    fun showEditReferenceView(reference: Reference)


    fun returnToPreviousView()

}