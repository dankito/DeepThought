package net.dankito.deepthought.javafx.routing

import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryExtractionResultView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleView
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.newsreader.model.EntryExtractionResult
import tornadofx.*


class JavaFXRouter(private val mainWindowController: MainWindowController) : IRouter {

    lateinit var entriesListView: IEntriesListView


    override fun showArticleSummaryExtractorsView() {
        // nothing to do on JavaFX
    }

    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        entriesListView.showEntriesForTag(tag, entries)
    }


    override fun showAddArticleSummaryExtractorView() {
        // TODO
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        runLater {
            val dialogView = mainWindowController.find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractor to extractor))
            dialogView.show(extractor.name, extractor.iconUrl)
        }
    }

    override fun showReadLaterArticlesView() {
        runLater {
            mainWindowController.find(ReadLaterArticleView::class).openWindow()
        }
    }


    override fun showViewEntryView(entry: Entry) {
        showEditEntryView(entry)
    }

    override fun showViewEntryView(extractionResult: EntryExtractionResult) {
        showEditEntryView(extractionResult)
    }


    override fun showCreateEntryView() {

    }

    override fun showEditEntryView(entry: Entry) {
        runLater {
            // TODO: set title when Reference is not set
            mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::entry to entry)).show(entry.referencePreview)
        }
    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult) {
        runLater {
            // TODO: set title when Reference is not set
            mainWindowController.find(EditEntryExtractionResultView::class, mapOf(EditEntryExtractionResultView::extractionResult to extractionResult)).show(extractionResult.reference?.preview)
        }
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}