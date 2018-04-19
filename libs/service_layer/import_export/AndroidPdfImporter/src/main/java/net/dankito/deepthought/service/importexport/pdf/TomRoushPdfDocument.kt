package net.dankito.deepthought.service.importexport.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File


class TomRoushPdfDocument(pdfFile: File) : IPdfDocument {

    val document = PDDocument.load(pdfFile)

    private val info = document.documentInformation


    override val numberOfPages = document.numberOfPages

    override val title = info.title ?: ""

    override val author = info.author ?: ""


    override fun close() {
        document.close()
    }

}