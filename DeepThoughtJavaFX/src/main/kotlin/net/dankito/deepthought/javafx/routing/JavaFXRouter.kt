package net.dankito.deepthought.javafx.routing

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryExtractionResultView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleView
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.newsreader.model.EntryExtractionResult


class JavaFXRouter(private val mainWindowController: MainWindowController) : IRouter {

    lateinit var entriesListView: IEntriesListView


    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        entriesListView.showEntriesForTag(tag, entries)
    }


    override fun showAddArticleSummaryExtractorView() {
        // TODO
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        mainWindowController.find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractor to extractor)).openWindow()
    }

    override fun showReadLaterArticlesView() {
        mainWindowController.find(ReadLaterArticleView::class).openWindow()
    }


    override fun showViewEntryView(entry: Entry) {
        // TODO: show Entry in MainWindow
    }

    override fun showViewEntryView(extractionResult: EntryExtractionResult) {
        // nothing like that on JavaFX, directly go to editing
    }

    override fun showEditEntryView(entry: Entry) {
        mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::entry to entry)).openWindow()
    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult) {
        mainWindowController.find(EditEntryExtractionResultView::class, mapOf(EditEntryExtractionResultView::extractionResult to extractionResult)).openWindow()
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}