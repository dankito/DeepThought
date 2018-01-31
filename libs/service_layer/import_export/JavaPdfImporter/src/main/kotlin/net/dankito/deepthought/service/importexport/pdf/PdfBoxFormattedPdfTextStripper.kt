package net.dankito.deepthought.service.importexport.pdf

import net.dankito.deepthought.service.importexport.pdf.IPdfTextStripper.Companion.ParagraphEnd
import net.dankito.deepthought.service.importexport.pdf.IPdfTextStripper.Companion.ParagraphStart
import net.dankito.deepthought.service.importexport.pdf.IPdfTextStripper.Companion.WordSeparator
import org.apache.pdfbox.util.PDFTextStripper


class PdfBoxFormattedPdfTextStripper : PDFTextStripper(), IPdfTextStripper {


    override var maxLineLength = -1

    override var lastLine = ""


    init {
        wordSeparator = WordSeparator // has no effect, is still using '\t'

        paragraphStart = ParagraphStart
        paragraphEnd = ParagraphEnd
    }


    override fun getText(document: IPdfDocument, startPage: Int, endPage: Int): String? {
        (document as? PdfBoxPdfDocument)?.let { documentWrapper ->
            this.startPage = startPage
            this.endPage = endPage

            return getText(documentWrapper.document)
        }

        return null
    }


    override fun writeString(text: String) {
        beforeWriteString(text)

        super.writeString(lastLine)
    }

    override fun writeLineSeparator() {
        lineSeparator = getLineSeparatorForLastLine()

        super.writeLineSeparator()
    }

}