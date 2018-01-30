package net.dankito.deepthought.service.importexport.pdf


data class FileMetadata(val countPages: Int, val title: String, val author: String, val error: Exception? = null)