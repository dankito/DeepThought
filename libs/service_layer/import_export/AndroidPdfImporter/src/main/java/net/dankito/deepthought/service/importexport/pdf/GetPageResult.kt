package net.dankito.deepthought.service.importexport.pdf


data class GetPageResult(val successful: Boolean, val page: String? = null, val error: Exception? = null)