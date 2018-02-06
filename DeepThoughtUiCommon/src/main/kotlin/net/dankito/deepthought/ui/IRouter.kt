package net.dankito.deepthought.ui

import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.model.ArticleSummary
import java.io.File


interface IRouter {

    fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>)

    fun showItemsForSource(source: Source)


    fun showArticleSummaryExtractorsView()

    fun showAddArticleSummaryExtractorView()

    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary? = null)

    fun showReadLaterArticlesView()


    fun showCreateItemView()

    fun showEditItemView(item: Item)

    fun showEditItemView(article: ReadLaterArticle)

    fun showEditItemView(extractionResult: ItemExtractionResult)


    fun showEditSourceView(source: Source)

    fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String? = null)


    fun showEditSeriesView(series: Series)

    fun showEditSourceSeriesView(forSource: Source, series: Series?)


    fun showPdfView(addNewPdfFile: File, sourceForFile: Source? = null)

    fun showPdfView(persistedPdfFile: FileLink, sourceForFile: Source? = null)


    fun returnToPreviousView()

}