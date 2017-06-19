package net.dankito.deepthought.javafx.routing

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.EntryOrExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.EntryExtractionResult


class JavaFXRouter(private val mainWindowController: MainWindowController) : IRouter {

    override fun showAddArticleSummaryExtractorView() {
        // TODO
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        mainWindowController.find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractor to extractor)).openWindow()
    }

    override fun showEntryView(entry: Entry) {
        mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::entryOrExtractionResult to EntryOrExtractionResult(entry, null))).openWindow()
    }

    override fun showEntryView(extractionResult: EntryExtractionResult) {
        mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::entryOrExtractionResult to EntryOrExtractionResult(null, extractionResult))).openWindow()
    }

    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}