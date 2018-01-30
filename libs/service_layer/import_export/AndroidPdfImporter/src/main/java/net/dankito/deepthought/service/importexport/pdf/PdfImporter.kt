package net.dankito.deepthought.service.importexport.pdf

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.io.File


class PdfImporter(applicationContext: Context, private val threadPool: IThreadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(PdfImporter::class.java)
    }


    private var currentDocument: PDDocument? = null

    private var pdfStripper: FormattedPDFTextStripper? = null


    init {
        PDFBoxResourceLoader.init(applicationContext)
    }


    fun loadFileAsync(file: File, loadingDone: (LoadPdfFileResult) -> Unit) {
        threadPool.runAsync {
            loadingDone(loadFile(file))
        }
    }

    fun loadFile(file: File): LoadPdfFileResult {
        close()

        try {
            val document = PDDocument.load(file)
            currentDocument = document

            pdfStripper = FormattedPDFTextStripper()

            val info = document.documentInformation
            return LoadPdfFileResult(true, FileMetadata(document.numberOfPages, info.title, info.author))
        } catch (e: Exception) {
            log.error("Could not load pdf file $file", e)

            return LoadPdfFileResult(false, error = e)
        }
    }


    fun getPageTextAsync(page: Int, done: (GetPageResult) -> Unit) {
        threadPool.runAsync {
            done(getPageText(page))
        }
    }

    fun getPageText(page: Int): GetPageResult {
        pdfStripper?.let { pdfStripper ->
            try {
                pdfStripper.startPage = page
                pdfStripper.endPage = page
                return GetPageResult(true, pdfStripper.getText(currentDocument))
            } catch(e: Exception) {
                log.error("Could not get text for page $page", e)

                return GetPageResult(false, error = e)
            }
        }

        return GetPageResult(false)
    }


    fun close() {
        currentDocument?.close()
        currentDocument == null

        pdfStripper = null
    }
}