package net.dankito.deepthought.service.importexport.pdf


data class LoadPdfFileResult(val successful: Boolean, val document: IPdfDocument? = null, val fileMetadata: FileMetadata? = null, val error: Exception? = null)