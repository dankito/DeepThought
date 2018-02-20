package net.dankito.deepthought.files.synchronization.message


data class PermitSynchronizeFileResponse(val result: PermitSynchronizeFileResult, val fileId: String?, val fileSize: Long?, var error: Exception? = null) {

    private constructor() : this(PermitSynchronizeFileResult.ErrorOccurred, null, null) // for Jackson

}