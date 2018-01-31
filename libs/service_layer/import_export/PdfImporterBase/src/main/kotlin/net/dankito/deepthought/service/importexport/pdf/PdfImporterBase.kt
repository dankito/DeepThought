package net.dankito.deepthought.service.importexport.pdf

import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.io.File


abstract class PdfImporterBase(private val threadPool: IThreadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(PdfImporterBase::class.java)
    }


    private var pdfStripper: IPdfTextStripper = createPdfTextStripper()


    abstract fun createPdfTextStripper(): IPdfTextStripper

    abstract fun loadDocument(file: File): IPdfDocument


    fun loadFileAsync(file: File, loadingDone: (LoadPdfFileResult) -> Unit) {
        threadPool.runAsync {
            loadingDone(loadFile(file))
        }
    }

    fun loadFile(file: File): LoadPdfFileResult {
        try {
            val document = loadDocument(file)

            return LoadPdfFileResult(true, document, FileMetadata(document.numberOfPages, document.title, document.author))
        } catch (e: Exception) {
            log.error("Could not load pdf file $file", e)

            return LoadPdfFileResult(false, error = e)
        }
    }


    fun getPageTextAsync(document: IPdfDocument, page: Int, done: (GetPageResult) -> Unit) {
        threadPool.runAsync {
            done(getPageText(document, page))
        }
    }

    fun getPageText(document: IPdfDocument, page: Int): GetPageResult {
        try {
            return GetPageResult(true, pdfStripper.getText(document, page, page))
        } catch(e: Exception) {
            log.error("Could not get text for page $page", e)

            return GetPageResult(false, error = e)
        }
    }

}