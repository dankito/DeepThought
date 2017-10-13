package net.dankito.deepthought.ui

import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.model.ArticleSummary


interface IRouter {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

    fun showEntriesForReference(source: Source)


    fun showArticleSummaryExtractorsView()

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary? = null)

    fun showReadLaterArticlesView()


    fun showCreateEntryView()

    fun showEditEntryView(item: Item)

    fun showEditEntryView(article: ReadLaterArticle)

    fun showEditEntryView(extractionResult: ItemExtractionResult)


    fun showEditReferenceView(source: Source)

    fun showEditEntryReferenceView(forItem: Item, source: Source?, series: Series?)


    fun showEditSeriesView(series: Series)

    fun showEditReferenceSeriesView(forSource: Source, series: Series?)


    fun returnToPreviousView()

}