package net.dankito.deepthought.javafx.routing

import javafx.stage.FileChooser
import net.dankito.deepthought.javafx.dialogs.articlesummary.ArticleSummaryView
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryWindowData
import net.dankito.deepthought.javafx.dialogs.item.EditItemExtractionResultView
import net.dankito.deepthought.javafx.dialogs.item.EditItemView
import net.dankito.deepthought.javafx.dialogs.item.EditReadLaterArticleView
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.pdf.ViewPdfDialog
import net.dankito.deepthought.javafx.dialogs.pdf.model.ViewPdfWindowData
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.ReadLaterArticleListView
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.javafx.dialogs.source.model.EditSourceWindowData
import net.dankito.deepthought.javafx.ui.controls.IItemsListViewJavaFX
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.utils.javafx.ui.image.JavaFXImageReference
import net.dankito.utils.windowregistry.window.javafx.ui.router.JavaFXRouterBase
import tornadofx.*
import java.io.File


class JavaFXRouter(private val mainWindowController: MainWindowController) : JavaFXRouterBase(mainWindowController), IRouter {

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
        showWindow(ArticleSummaryView::class.java, ArticleSummaryWindowData(extractor, summary), extractor.name, extractor.iconUrl?.let { JavaFXImageReference(it) })
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
        showWindow(EditItemView::class.java, item, title)
    }

    override fun showEditItemView(article: ReadLaterArticle) {
        showWindow(EditReadLaterArticleView::class.java, article, article.itemExtractionResult.source?.preview)
    }

    override fun showEditItemView(extractionResult: ItemExtractionResult) {
        showWindow(EditItemExtractionResultView::class.java, extractionResult, extractionResult.source?.preview)
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
        doShowEditItemSourceView(source)
    }

    override fun showEditItemSourceView(source: Source?, series: Series?, editedSourceTitle: String?) {
        val sourceToEdit = source ?: Source("")

        doShowEditItemSourceView(sourceToEdit, series, editedSourceTitle)
    }

    private fun doShowEditItemSourceView(source: Source, series: Series? = null, editedSourceTitle: String? = null) {
        val title = String.format(FX.messages["edit.source.dialog.title"], editedSourceTitle ?: source.title)

        showWindow(EditSourceDialog::class.java, EditSourceWindowData(source, series, editedSourceTitle), title)
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

    override fun showPdfView(persistedPdfFile: FileLink, sourceForFile: Source?) {
        showImportFromPdfView(null, persistedPdfFile, sourceForFile)
    }

    private fun showImportFromPdfView(addNewPdfFile: File?, persistedPdfFile: FileLink?, sourceForFile: Source?) {
        val title = String.format(FX.messages["view.pdf.dialog.title"], addNewPdfFile?.name ?: persistedPdfFile?.name)

        showWindow(ViewPdfDialog::class.java, ViewPdfWindowData(addNewPdfFile, persistedPdfFile, sourceForFile), title)
    }


    override fun returnToPreviousView() {
        // there's no such thing as go to previous view in JavaFX, simply close the dialog
    }

}