package net.dankito.deepthought.javafx.routing

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryExtractionResultView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryView
import net.dankito.deepthought.javafx.dialogs.entry.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleListView
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.newsreader.model.ArticleSummary
import tornadofx.*


class JavaFXRouter(private val mainWindowController: MainWindowController) : IRouter {

    lateinit var entriesListView: IEntriesListView


    override fun showArticleSummaryExtractorsView() {
        // nothing to do on JavaFX
    }

    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        entriesListView.showEntriesForTag(tag, tagsFilter)
    }

    override fun showEntriesForReference(reference: Reference) {
        // TODO
    }


    override fun showAddArticleSummaryExtractorView() {
        // TODO
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary?) {
        runLater {
            // TODO: may also pass summary
            val dialogView = mainWindowController.find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractor to extractor))
            dialogView.show(extractor.name, extractor.iconUrl)
        }
    }

    override fun showReadLaterArticlesView() {
        runLater {
            mainWindowController.find(ReadLaterArticleListView::class).openWindow()
        }
    }


    override fun showCreateEntryView() {
        showEditEntryView(Entry(""), FX.messages["create.entry.window.title"])
    }

    override fun showEditEntryView(entry: Entry) {
        // TODO: set title when Reference is not set
        showEditEntryView(entry, entry.reference.previewWithSeriesAndPublishingDate)
    }

    private fun showEditEntryView(entry: Entry, title: String?) {
        runLater {
            mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::entry to entry)).show(title)
        }
    }

    override fun showEditEntryView(article: ReadLaterArticle) {
        runLater {
            // TODO: set title when Reference is not set
            mainWindowController.find(EditReadLaterArticleView::class, mapOf(EditReadLaterArticleView::article to article)).show(article.entryExtractionResult.reference?.preview)
        }
    }

    override fun showEditEntryView(extractionResult: EntryExtractionResult) {
        runLater {
            // TODO: set title when Reference is not set
            mainWindowController.find(EditEntryExtractionResultView::class, mapOf(EditEntryExtractionResultView::extractionResult to extractionResult)).show(extractionResult.reference?.preview)
        }
    }


    override fun showEditReferenceView(reference: Reference) {
        // TODO
    }

    override fun showEditEntryReferenceView(forEntry: Entry, reference: Reference?, series: Series?) {
        // there should be no need for this on JavaFX
    }


    override fun showEditSeriesView(series: Series) {
        // TODO
    }

    override fun showEditReferenceSeriesView(forReference: Reference, series: Series?) {
        // there should be no need for this on JavaFX
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}