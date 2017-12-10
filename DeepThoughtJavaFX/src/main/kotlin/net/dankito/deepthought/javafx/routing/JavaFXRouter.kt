package net.dankito.deepthought.javafx.routing

import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryExtractionResultView
import net.dankito.deepthought.javafx.dialogs.entry.EditEntryView
import net.dankito.deepthought.javafx.dialogs.entry.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleListView
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
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

    override fun showEntriesForReference(source: Source) {
        // TODO
    }


    override fun showAddArticleSummaryExtractorView() {
        // TODO
    }

    override fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig, summary: ArticleSummary?) {
        runLater {
            // TODO: may also pass summary
            val dialogView = mainWindowController.find(ArticleSummaryView::class, mapOf(ArticleSummaryView::articleSummaryExtractorConfig to extractor))
            dialogView.show(extractor.name, extractor.iconUrl)
        }
    }

    override fun showReadLaterArticlesView() {
        runLater {
            mainWindowController.find(ReadLaterArticleListView::class).openWindow()
        }
    }


    override fun showCreateEntryView() {
        showEditEntryView(Item(""), FX.messages["create.item.window.title"])
    }

    override fun showEditEntryView(item: Item) {
        // TODO: set title when Source is not set
        showEditEntryView(item, item.source.previewWithSeriesAndPublishingDate)
    }

    private fun showEditEntryView(item: Item, title: String?) {
        runLater {
            mainWindowController.find(EditEntryView::class, mapOf(EditEntryView::item to item)).show(title)
        }
    }

    override fun showEditEntryView(article: ReadLaterArticle) {
        runLater {
            // TODO: set title when Source is not set
            mainWindowController.find(EditReadLaterArticleView::class, mapOf(EditReadLaterArticleView::article to article)).show(article.itemExtractionResult.source?.preview)
        }
    }

    override fun showEditEntryView(extractionResult: ItemExtractionResult) {
        runLater {
            // TODO: set title when Source is not set
            mainWindowController.find(EditEntryExtractionResultView::class, mapOf(EditEntryExtractionResultView::extractionResult to extractionResult)).show(extractionResult.source?.preview)
        }
    }


    override fun showEditReferenceView(source: Source) {
        mainWindowController.find(EditSourceDialog::class, mapOf(EditSourceDialog::source to source)).show(getEditSourceDialogTitle(source.title))
    }

    override fun showEditEntryReferenceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        // TODO: set title when Source is not set
        mainWindowController.find(EditSourceDialog::class, mapOf(EditSourceDialog::source to (source ?: Source("")))).show(getEditSourceDialogTitle(source?.title ?: ""))
    }

    private fun getEditSourceDialogTitle(sourceTitle: String) = String.format(FX.messages["edit.source.dialog.title"], sourceTitle)


    override fun showEditSeriesView(series: Series) {
        // TODO
    }

    override fun showEditReferenceSeriesView(forSource: Source, series: Series?) {
        // there should be no need for this on JavaFX
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}