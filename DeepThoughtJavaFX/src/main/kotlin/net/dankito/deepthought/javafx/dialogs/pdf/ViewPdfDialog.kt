package net.dankito.deepthought.javafx.dialogs.pdf

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.res.icons.Icons
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.importexport.pdf.FileMetadata
import net.dankito.deepthought.service.importexport.pdf.GetPageResult
import net.dankito.deepthought.service.importexport.pdf.IPdfDocument
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.localization.Localization
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import javax.inject.Inject


class ViewPdfDialog : DialogFragment() {

    companion object {
        private const val ButtonsSize = 36.0
        private const val PreviousAndNextButtonsLeftRightMargin = 12.0
        private const val PageFieldWidth = 55.0
        private const val FontSize = 16.0

        private val FileNullObject = File(".")
        private val FileLinkNullObject = FileLink(".")
        private val SourceNullObject = Source()

        private val logger = LoggerFactory.getLogger(ViewPdfDialog::class.java)
    }


    @Inject
    protected lateinit var importer: PdfImporter

    @Inject
    protected lateinit var fileManager: FileManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var localization: Localization



    val addNewPdfFileParam: File? by param(FileNullObject) // by param() doesn't seem to like when passing null - on calling get() an exception gets thrown

    private var addNewPdfFile: File? = if(addNewPdfFileParam == FileNullObject) null else addNewPdfFileParam

    val persistedPdfFileParam: FileLink? by param(FileLinkNullObject) // by param() doesn't seem to like when passing null - on calling get() an exception gets thrown

    private var persistedPdfFile: FileLink? = if(persistedPdfFileParam == FileLinkNullObject) null else persistedPdfFileParam

    val sourceForFileParam: Source? by param(SourceNullObject) // by param() doesn't seem to like when passing null - on calling get() an exception gets thrown

    private var sourceForFile: Source? = if(sourceForFileParam == SourceNullObject) null else sourceForFileParam


    private val currentPageText = SimpleStringProperty("")

    private val selectedText = SimpleStringProperty("")

    private val textCountPages = SimpleStringProperty("")

    private val textCurrentPage = SimpleStringProperty("")

    private val isPdfFileLoaded = SimpleBooleanProperty(false)

    private val canNavigateToPreviousPage = SimpleBooleanProperty(false)

    private val canNavigateToNextPage = SimpleBooleanProperty(false)

    private var currentPage = -1

    private var pdfDocument: IPdfDocument? = null

    private var fileMetaData: FileMetadata? = null


    init {
        AppComponent.component.inject(this)

        addNewPdfFile?.let { loadPdf(it) }

        persistedPdfFile?.let { loadPdf(it) }
    }


    override val root = borderpane {
        prefHeight = 600.0
        prefWidth = 850.0

        center = textarea(currentPageText) {
            vgrow = Priority.ALWAYS
            useMaxHeight = true

            isWrapText = true
            isEditable = false // to make it look like a Label (there's no way to select text in a Label, see https://stackoverflow.com/questions/22534067/copiable-label-textfield-labeledtext-in-javafx)
            FXUtils.setBackgroundToColor(this, Color.TRANSPARENT)

            this@ViewPdfDialog.selectedText.bind(selectedTextProperty())

            borderpaneConstraints {
                margin = Insets(12.0)
            }
        }

        bottom = borderpane {
            center = hbox {
                alignment = Pos.CENTER
                prefHeight = ButtonsSize

                button("", ImageView(Icons.NavigatePreviousIconPath)) {
                    setButtonSize()
                    disableProperty().bind(canNavigateToPreviousPage.not())

                    action {
                        if(currentPage > 0) {
                            loadPageTextOnUiThread(currentPage - 1)
                        }
                    }

                    hboxConstraints {
                        marginRight = PreviousAndNextButtonsLeftRightMargin
                    }
                }

                textfield(textCurrentPage) {
                    minHeight = ButtonsSize - 6.0
                    prefWidth = PageFieldWidth
                    alignment = Pos.CENTER
                    font = Font.font(FontSize)
                    disableProperty().bind(isPdfFileLoaded.not())

                    focusedProperty().addListener { _, _, newValue -> if(newValue == false) tryToLoadEnteredPage(text) }
                    setOnKeyReleased { event -> if(event.code == KeyCode.ENTER) tryToLoadEnteredPage(text) }
                }

                label("/") {
                    font = Font.font(FontSize)

                    hboxConstraints {
                        marginLeftRight(8.0)
                    }
                }

                label(textCountPages) {
                    prefWidth = PageFieldWidth
                    alignment = Pos.CENTER
                    font = Font.font(FontSize)
                }

                button("", ImageView(Icons.NavigateNextIconPath)) {
                    setButtonSize()
                    disableProperty().bind(canNavigateToNextPage.not())

                    action {
                        fileMetaData?.let { metaData ->
                            if(currentPage < metaData.countPages) {
                                loadPageTextOnUiThread(currentPage + 1)
                            }
                        }
                    }

                    hboxConstraints {
                        marginLeft = PreviousAndNextButtonsLeftRightMargin
                    }
                }
            }

            right = button("", ImageView(Icons.ItemsIconPath)) {
                setButtonSize()
                disableProperty().bind(selectedText.isEmpty)

                action { createItemFromSelectedText() }
            }
        }
    }

    private fun Button.setButtonSize() {
        minHeight = ButtonsSize
        maxHeight = ButtonsSize
        minWidth = ButtonsSize
        maxWidth = ButtonsSize
    }


    override fun onUndock() {
        pdfDocument?.close()

        super.onUndock()
    }


    private fun loadPdf(pdfFile: File) {
        loadPdf(fileManager.createLocalFile(pdfFile))
    }

    private fun loadPdf(pdfFile: FileLink) {
        val localFile = fileManager.getLocalPathForFile(pdfFile)

        importer.loadFileAsync(localFile) { result ->
            this.pdfDocument = result.document

            result.fileMetadata?.let {
                runLater { loadedFileOnUiThread(pdfFile, it) }
            }

            result.error?.let {
                runLater { couldNotLoadPdfOnUiThread(localFile, it) }
            }
        }
    }

    private fun loadedFileOnUiThread(pdfFile: FileLink, metadata: FileMetadata) {
        fileMetaData = metadata

        if(sourceForFile == null) {
            sourceForFile = createSource(metadata, pdfFile)
        }

        textCountPages.value = metadata.countPages.toString()
        setControlsEnabledState(true)

        loadPageTextOnUiThread(1)
    }

    private fun couldNotLoadPdfOnUiThread(localFile: File, exception: Exception) {
        currentPageText.value = localization.getLocalizedString("file.page.could.not.load.file", localFile.absolutePath, exception)

        textCountPages.value = ""
        setControlsEnabledState(false)
    }

    private fun setControlsEnabledState(isPdfFileLoaded: Boolean) {
        this.isPdfFileLoaded.value = isPdfFileLoaded

        canNavigateToPreviousPage.value = isPdfFileLoaded && currentPage > 1
        canNavigateToNextPage.value = isPdfFileLoaded && currentPage > 0 && currentPage < (fileMetaData?.countPages ?: 0)
    }

    private fun createSource(metadata: FileMetadata, pdfFile: FileLink): Source {
        val sourceTitle = (if (metadata.author.isNotBlank()) metadata.author + " - " else "") + metadata.title
        val source = Source(sourceTitle)
        source.length = localization.getLocalizedString("file.count.pages", metadata.countPages)

        source.addAttachedFile(pdfFile)

        return source
    }


    private fun tryToLoadEnteredPage(enteredText: String) {
        try {
            fileMetaData?.let { metaData ->
                val page = enteredText.toInt()
                if(page > 0 && page <= metaData.countPages) {
                    loadPageTextOnUiThread(page)
                }
            }
        } catch (e: Exception) {
            logger.error("Could not load page $enteredText", e)
        }
    }

    private fun loadPageTextOnUiThread(page: Int) {
        pdfDocument?.let { pdfDocument ->
            importer.getPageTextAsync(pdfDocument, page) { result ->
                runLater { pageTextLoadedOnUiThread(page, result) }
            }
        }
    }

    private fun pageTextLoadedOnUiThread(page: Int, result: GetPageResult) {
        currentPage = page
        textCurrentPage.value = page.toString()

        result.page?.let {
            currentPageText.value = it
        }

        result.error?.let {
            currentPageText.value = localization.getLocalizedString("file.page.could.not.load.page", page, it)
        }

        setControlsEnabledState(true)
    }


    private fun createItemFromSelectedText() {
        val item = Item(selectedText.value)

        item.indication =
                if(fileMetaData != null) localization.getLocalizedString("file.page.indication.with.count.pages.known", currentPage, fileMetaData?.countPages ?: 0)
                else localization.getLocalizedString("file.page.indication", currentPage)

        router.showEditEntryView(ItemExtractionResult(item, sourceForFile, couldExtractContent = true))
    }

}