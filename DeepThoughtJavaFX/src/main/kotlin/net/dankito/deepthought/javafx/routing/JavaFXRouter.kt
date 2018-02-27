package net.dankito.deepthought.javafx.routing

import javafx.stage.FileChooser
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.item.EditItemExtractionResultView
import net.dankito.deepthought.javafx.dialogs.item.EditItemView
import net.dankito.deepthought.javafx.dialogs.item.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.pdf.ViewPdfDialog
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleListView
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.javafx.ui.controls.IItemsListViewJavaFX
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.ArticleSummary
import tornadofx.*
import java.io.File


class JavaFXRouter(private val mainWindowController: MainWindowController) : IRouter {

    lateinit var itemsListView: IItemsListViewJavaFX


    override fun showArticleSummaryExtractorsView() {
        // nothing to do on JavaFX
    }

    override fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>) {
        itemsListView.showItemsForTag(tag, tagsFilter)
    }

    override fun showItemsForSource(source: Source) {
        itemsListView.showItemsForSource(source)
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


    override fun showCreateItemView() {
        showEditItemView(Item(""), FX.messages["create.item.window.title"])
    }

    override fun showEditItemView(item: Item) {
        // TODO: set title when Source is not set
        showEditItemView(item, item.source.previewWithSeriesAndPublishingDate)
    }

    private fun showEditItemView(item: Item, title: String?) {
        runLater {
            mainWindowController.find(EditItemView::class, mapOf(EditItemView::item to item)).show(title)
        }
    }

    override fun showEditItemView(article: ReadLaterArticle) {
        runLater {
            // TODO: set title when Source is not set
            mainWindowController.find(EditReadLaterArticleView::class, mapOf(EditReadLaterArticleView::article to article)).show(article.itemExtractionResult.source?.preview)
        }
    }

    override fun showEditItemView(extractionResult: ItemExtractionResult) {
        runLater {
            // TODO: set title when Source is not set
            mainWindowController.find(EditItemExtractionResultView::class, mapOf(EditItemExtractionResultView::extractionResult to extractionResult)).show(extractionResult.source?.preview)
        }
    }


    override fun createItemFromPdf() {
        selectFileToOpen("new.item.from.pdf",
                FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf", "*.PDF"))?.let { pdfFile ->
            showPdfView(pdfFile)
        }
    }

    private fun selectFileToOpen(titleResourceKey: String? = null, vararg extensionFilter: FileChooser.ExtensionFilter): File? {
        val chooser = FileChooser()

        titleResourceKey?.let {
            chooser.title = FX.messages[it]
        }

        chooser.extensionFilters.addAll(extensionFilter)

        return chooser.showOpenDialog(FX.primaryStage) // TODO: get current window
    }


    override fun showEditSourceView(source: Source) {
        runLater {
            mainWindowController.find(EditSourceDialog::class, mapOf(EditSourceDialog::source to source)).show(getEditSourceDialogTitle(source.title))
        }
    }

    override fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        runLater {
            val sourceToEdit = source ?: Source("")

            mainWindowController.find(EditSourceDialog::class,
                    mapOf(EditSourceDialog::source to sourceToEdit, EditSourceDialog::seriesParam to series, EditSourceDialog::editedSourceTitle to editedSourceTitle)
            ).show(getEditSourceDialogTitle(sourceToEdit.title, editedSourceTitle))
        }
    }

    private fun getEditSourceDialogTitle(sourceTitle: String, editedSourceTitle: String? = null): String {
        return String.format(FX.messages["edit.source.dialog.title"], editedSourceTitle ?: sourceTitle)
    }


    override fun showEditSeriesView(series: Series) {
        // TODO
    }

    override fun showEditSourceSeriesView(forSource: Source, series: Series?) {
        // there should be no need for this on JavaFX
    }


    override fun showPdfView(addNewPdfFile: File, sourceForFile: Source?) {
        showImportFromPdfView(addNewPdfFile, null, sourceForFile)
    }

    override fun showPdfView(persistedPdfFile: DeepThoughtFileLink, sourceForFile: Source?) {
        showImportFromPdfView(null, persistedPdfFile, sourceForFile)
    }

    private fun showImportFromPdfView(addNewPdfFile: File?, persistedPdfFile: DeepThoughtFileLink?, sourceForFile: Source?) {
        runLater {
            mainWindowController.find(ViewPdfDialog::class,
                    mapOf(ViewPdfDialog::addNewPdfFileParam to addNewPdfFile, ViewPdfDialog::persistedPdfFileParam to persistedPdfFile,
                            ViewPdfDialog::sourceForFileParam to sourceForFile)
            ).show(getViewPdfDialogTitle(addNewPdfFile, persistedPdfFile))
        }
    }

    private fun getViewPdfDialogTitle(addNewPdfFile: File?, persistedPdfFile: DeepThoughtFileLink?): String {
        return String.format(FX.messages["view.pdf.dialog.title"], addNewPdfFile?.name ?: persistedPdfFile?.name)
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}