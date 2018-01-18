package net.dankito.deepthought.files.synchronization


data class PermitSynchronizeFileResponse(val result: PermitSynchronizeFileResult, val fileId: String?, var error: Exception? = null) {

    private constructor() : this(PermitSynchronizeFileResult.ErrorOccurred, null) // for Jackson

}