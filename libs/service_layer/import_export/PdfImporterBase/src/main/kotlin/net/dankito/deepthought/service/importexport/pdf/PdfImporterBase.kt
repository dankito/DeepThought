package net.dankito.deepthought.service.importexport.pdf

import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.io.File


abstract class PdfImporterBase(private val threadPool: IThreadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(PdfImporterBase::class.java)
    }


    protected var currentDocument: IPdfDocument? = null

    protected var pdfStripper: IPdfTextStripper? = null


    fun loadFileAsync(file: File, loadingDone: (LoadPdfFileResult) -> Unit) {
        threadPool.runAsync {
            loadingDone(loadFile(file))
        }
    }

    fun loadFile(file: File): LoadPdfFileResult {
        close()

        try {
            val document = loadDocument(file)
            currentDocument = document

            pdfStripper = createPdfTextStripper()

            return LoadPdfFileResult(true, FileMetadata(document.numberOfPages, document.title, document.author))
        } catch (e: Exception) {
            log.error("Could not load pdf file $file", e)

            return LoadPdfFileResult(false, error = e)
        }
    }

    abstract fun createPdfTextStripper(): IPdfTextStripper

    abstract fun loadDocument(file: File): IPdfDocument


    fun getPageTextAsync(page: Int, done: (GetPageResult) -> Unit) {
        threadPool.runAsync {
            done(getPageText(page))
        }
    }

    fun getPageText(page: Int): GetPageResult {
        pdfStripper?.let { pdfStripper ->
            currentDocument?.let { currentDocument ->
                try {
                    return GetPageResult(true, pdfStripper.getText(currentDocument, page, page))
                } catch(e: Exception) {
                    log.error("Could not get text for page $page", e)

                    return GetPageResult(false, error = e)
                }
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