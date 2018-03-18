package net.dankito.deepthought.service.importexport.pdf


interface IPdfDocument {

    val numberOfPages: Int

    val title: String

    val author: String


    fun close()

}