package net.dankito.deepthought.service.importexport.pdf


interface IPdfTextStripper {

    companion object {
        const val WordSeparator = " "

        const val ParagraphStart = "\n"

        const val ParagraphEnd = "\n"
    }


    var maxLineLength: Int

    var lastLine: String

    fun getText(document: IPdfDocument, startPage: Int, endPage: Int): String?


    fun beforeWriteString(text: String) {
        lastLine = text.replace('\t', ' ')
        maxLineLength = Math.max(lastLine.length, maxLineLength)
    }

    fun getLineSeparatorForLastLine(): String {
        if(maxLineLength > 0 && lastLine.length >= 0.75 * maxLineLength) {
            return " " // just a new line
        }
        else {
            return "\n" // paragraph end
        }
    }

}