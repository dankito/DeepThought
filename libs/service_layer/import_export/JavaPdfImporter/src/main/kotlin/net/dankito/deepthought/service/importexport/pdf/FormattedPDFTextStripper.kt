package net.dankito.deepthought.service.importexport.pdf

import org.apache.pdfbox.text.PDFTextStripper


class FormattedPDFTextStripper : PDFTextStripper() {

    var maxLineLength = -1

    var lastLine = ""


    init {
        wordSeparator = " " // has no effect, is still using '\t'

        paragraphStart = "\n"
        paragraphEnd = "\n"
    }

    override fun writeString(text: String) {
        lastLine = text.replace('\t', ' ')
        maxLineLength = Math.max(lastLine.length, maxLineLength)

        super.writeString(lastLine)
    }

    override fun writeLineSeparator() {
        lineSeparator =
                if(maxLineLength > 0 && lastLine.length >= 0.75 * maxLineLength) " " // just a new line
                else "\n" // paragraph end

        super.writeLineSeparator()
    }

}