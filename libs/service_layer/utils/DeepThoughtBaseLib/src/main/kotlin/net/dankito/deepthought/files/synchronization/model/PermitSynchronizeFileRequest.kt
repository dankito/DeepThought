package net.dankito.deepthought.files.synchronization.model


data class PermitSynchronizeFileRequest(val fileId: String) {

    private constructor() : this("") // for Jackson

}