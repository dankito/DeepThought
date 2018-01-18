package net.dankito.deepthought.files.synchronization


data class PermitSynchronizeFileRequest(val fileId: String) {

    private constructor() : this("") // for Jackson

}