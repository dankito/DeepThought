package net.dankito.deepthought.android.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_view_pdf.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.ViewPdfActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.importexport.pdf.FileMetadata
import net.dankito.deepthought.service.importexport.pdf.GetPageResult
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.localization.Localization
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject


class ViewPdfActivity : BaseActivity() {

    companion object {
        private val log = LoggerFactory.getLogger(ViewPdfActivity::class.java)
    }


    @Inject
    protected lateinit var importer: PdfImporter

    @Inject
    protected lateinit var fileManager: FileManager

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var localization: Localization


    private var currentPage = -1

    private var fileMetaData: FileMetadata? = null

    private var sourceForFile: Source? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUi()

        if(savedInstanceState == null) {
            showParameters(getParameters() as? ViewPdfActivityParameters)
        }
    }

    private fun setupUi() {
        setContentView(R.layout.activity_view_pdf)

        txtPageText.setOnTouchListener { _, _ ->
            btnCreateItemFromSelectedText.isEnabled = txtPageText.hasSelection()
            false
        }

        btnNavigateToPreviousPage.setOnClickListener {
            if(currentPage > 0) {
                loadPageTextOnUiThread(currentPage - 1)
            }
        }

        btnNavigateToNextPage.setOnClickListener {
            fileMetaData?.let { metaData ->
                if(currentPage < metaData.countPages) {
                    loadPageTextOnUiThread(currentPage + 1)
                }
            }
        }

        edtxtCurrentPage.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus == false) {
                tryToLoadEnteredPage()
            }
        }

        edtxtCurrentPage.setOnEditorActionListener { _, actionId, event ->
            tryToLoadEnteredPage()
            edtxtCurrentPage.hideKeyboard()
            txtPageText.requestFocus()
            true
        }

        btnCreateItemFromSelectedText.setOnClickListener {
            createItemFromSelectedText()
        }

        edtxtCurrentPage.clearFocus()
        txtPageText.requestFocus()
    }


    private fun showParameters(parameters: ViewPdfActivityParameters?) {
        parameters?.let {
            this.sourceForFile = parameters.sourceForFile

            parameters.persistedPdfFile?.let {
                loadPdf(it)
            }

            parameters.addNewPdfFile?.let {
                loadPdf(it)
            }
        }
    }

    private fun loadPdf(pdfFile: File) {
        loadPdf(fileManager.createLocalFile(pdfFile))
    }

    private fun loadPdf(pdfFile: FileLink) {
        val localFile = fileManager.getLocalPathForFile(pdfFile)

        importer.loadFileAsync(localFile) { result ->
            result.fileMetadata?.let {
                runOnUiThread { loadedFileOnUiThread(pdfFile, it) }
            }

            result.error?.let {
                runOnUiThread { couldNotLoadPdfOnUiThread(localFile, it) }
            }
        }
    }

    private fun loadedFileOnUiThread(pdfFile: FileLink, metadata: FileMetadata) {
        fileMetaData = metadata

        if(sourceForFile == null) {
            sourceForFile = createSource(metadata, pdfFile)
        }

        txtCountPages.text = metadata.countPages.toString()
        setControlsEnabledState(true)

        loadPageTextOnUiThread(1)
    }

    private fun couldNotLoadPdfOnUiThread(localFile: File, exception: Exception) {
        txtPageText.text = localization.getLocalizedString("file.page.could.not.load.file", localFile.absolutePath, exception)

        txtCountPages.text = ""
        setControlsEnabledState(false)
    }

    private fun setControlsEnabledState(areEnabled: Boolean, isButtonCreateItemFromSelectedTextEnabled: Boolean = areEnabled && txtPageText.hasSelection()) {
        edtxtCurrentPage.isEnabled = areEnabled
        btnNavigateToPreviousPage.isEnabled = areEnabled && currentPage > 1
        btnNavigateToNextPage.isEnabled = areEnabled && currentPage > 0 && currentPage < (fileMetaData?.countPages ?: 0)
        btnCreateItemFromSelectedText.isEnabled = isButtonCreateItemFromSelectedTextEnabled
    }

    private fun createSource(metadata: FileMetadata, pdfFile: FileLink): Source {
        val sourceTitle = (if (metadata.author.isNotBlank()) metadata.author + " - " else "") + metadata.title
        val source = Source(sourceTitle)
        source.length = localization.getLocalizedString("file.count.pages", metadata.countPages)

        source.addAttachedFile(pdfFile)

        return source
    }


    private fun tryToLoadEnteredPage() {
        try {
            fileMetaData?.let { metaData ->
                val page = edtxtCurrentPage.text.toString().toInt()
                if(page > 0 && page <= metaData.countPages) {
                    loadPageTextOnUiThread(page)
                }
            }
        } catch (e: Exception) {
            log.error("Could not load page ${edtxtCurrentPage.text}", e)
        }
    }

    private fun loadPageTextOnUiThread(page: Int) {
        importer.getPageTextAsync(page) { result ->
            runOnUiThread { pageTextLoadedOnUiThread(page, result) }
        }
    }

    private fun pageTextLoadedOnUiThread(page: Int, result: GetPageResult) {
        currentPage = page
        edtxtCurrentPage.setText(page.toString())

        result.page?.let {
            txtPageText.text = it
        }

        result.error?.let {
            txtPageText.text = localization.getLocalizedString("file.page.could.not.load.page", page, it)
        }

        setControlsEnabledState(true)
    }


    private fun createItemFromSelectedText() {
        val selectedText = txtPageText.text.substring(txtPageText.selectionStart, txtPageText.selectionEnd)
        val item = Item(selectedText)

        item.indication =
                if (fileMetaData != null) localization.getLocalizedString("file.page.indication.with.count.pages.known", currentPage, fileMetaData?.countPages ?: 0)
                else localization.getLocalizedString("file.page.indication", currentPage)

        router.showEditEntryView(ItemExtractionResult(item, sourceForFile, couldExtractContent = true))
    }

}
