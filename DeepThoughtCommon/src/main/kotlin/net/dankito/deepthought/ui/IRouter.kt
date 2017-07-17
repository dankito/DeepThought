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


    fun showViewEntryView(entry: Entry)

    fun showViewEntryView(article: ReadLaterArticle)

    fun showViewEntryView(extractionResult: EntryExtractionResult)


    fun showCreateEntryView()

    fun showEditEntryView(entry: Entry)

    fun showEditEntryView(article: ReadLaterArticle)

    fun showEditEntryView(extractionResult: EntryExtractionResult)


    fun returnToPreviousView()

}