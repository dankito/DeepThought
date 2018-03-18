package net.dankito.deepthought.service.importexport.pdf

import net.dankito.utils.IThreadPool
import java.io.File


class PdfImporter(threadPool: IThreadPool) : PdfImporterBase(threadPool) {

    override fun loadDocument(file: File): IPdfDocument {
        return PdfBoxPdfDocument(file)
    }

    override fun createPdfTextStripper(): IPdfTextStripper {
        return PdfBoxFormattedPdfTextStripper()
    }

}