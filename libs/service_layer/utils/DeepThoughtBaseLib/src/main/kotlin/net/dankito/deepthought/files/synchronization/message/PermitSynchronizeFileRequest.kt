package net.dankito.deepthought.files.synchronization.message


data class PermitSynchronizeFileRequest(val fileId: String) {

    private constructor() : this("") // for Jackson

}