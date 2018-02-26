package net.dankito.deepthought.service.importexport.pdf

import android.content.Context
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import net.dankito.util.IThreadPool
import java.io.File


class PdfImporter(applicationContext: Context, threadPool: IThreadPool) : PdfImporterBase(threadPool) {


    init {
        PDFBoxResourceLoader.init(applicationContext)
    }


    override fun loadDocument(file: File): IPdfDocument {
        return TomRoushPdfDocument(file)
    }

    override fun createPdfTextStripper(): IPdfTextStripper {
        return TomRoushFormattedPdfTextStripper()
    }

}