package net.dankito.deepthought.android.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_view_pdf.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.ViewPdfActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.data.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.importexport.pdf.FileMetadata
import net.dankito.deepthought.service.importexport.pdf.GetPageResult
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.FileService
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
    protected lateinit var fileService: FileService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var fileManager: FileManager


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
            val selectedText = txtPageText.text.substring(txtPageText.selectionStart, txtPageText.selectionEnd)
            val item = Item(selectedText)
            item.indication = currentPage.toString() + (if(fileMetaData != null) { " / " + fileMetaData?.countPages } else "")
            router.showEditEntryView(ItemExtractionResult(item, sourceForFile, couldExtractContent = true))
        }

        edtxtCurrentPage.clearFocus()
        txtPageText.requestFocus()
    }


    private fun showParameters(parameters: ViewPdfActivityParameters?) {
        parameters?.let {
            this.sourceForFile = parameters.sourceForFile

            loadPdf(parameters.pdfFile)
        }
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
        setControlsEnabledState(true, txtPageText.hasSelection())

        loadPageTextOnUiThread(1)
    }

    private fun couldNotLoadPdfOnUiThread(localFile: File, exception: Exception) {
        txtPageText.text = "Could not load PDF file ${localFile.absolutePath}:\n\n$exception"

        txtCountPages.text = ""
        setControlsEnabledState(false)
    }

    private fun setControlsEnabledState(areEnabled: Boolean, isButtonCreateItemFromSelectedTextEnabled: Boolean = areEnabled) {
        edtxtCurrentPage.isEnabled = areEnabled
        btnNavigateToPreviousPage.isEnabled = areEnabled
        btnNavigateToNextPage.isEnabled = areEnabled
        btnCreateItemFromSelectedText.isEnabled = isButtonCreateItemFromSelectedTextEnabled
    }

    private fun createSource(metadata: FileMetadata, pdfFile: FileLink): Source {
        val sourceTitle = (if (metadata.author.isNotBlank()) metadata.author + " - " else "") + metadata.title
        val source = Source(sourceTitle)
        source.length = resources.getString(R.string.activity_view_pdf_source_length_in_pages, metadata.countPages)

        if(pdfFile.isPersisted() == false) {
            fileService.persist(pdfFile) // TODO: integrate in ReferencePersister
        }

        source.addAttachedFile(pdfFile)

        return source
    }


    private fun tryToLoadEnteredPage() {
        try {
            fileMetaData?.let { metaData ->
                val page = edtxtCurrentPage.text.toString().toInt()
                if (page >= 0 && page < metaData.countPages) {
                    loadPageTextOnUiThread(page)
                }
            }
        } catch (e: Exception) {
            log.error("Could not load page ${edtxtCurrentPage.text}", e)
        }
    }

    private fun loadPageTextOnUiThread(page: Int) {
        currentPage = page
        edtxtCurrentPage.setText(page.toString())

        importer.getPageTextAsync(page) { result ->
            runOnUiThread { pageTextLoadedOnUiThread(page, result) }
        }
    }

    private fun pageTextLoadedOnUiThread(page: Int, result: GetPageResult) {
        result.page?.let {
            txtPageText.text = it
        }

        result.error?.let {
            txtPageText.text = "Could not load text of page $page: $it"
        }
    }

}
