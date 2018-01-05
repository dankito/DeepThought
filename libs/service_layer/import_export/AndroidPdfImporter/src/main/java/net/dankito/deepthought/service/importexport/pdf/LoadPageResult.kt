package net.dankito.deepthought.service.importexport.pdf


data class LoadPageResult(val successful: Boolean, val fileMetadata: FileMetadata? = null, val error: Exception? = null)