package net.dankito.synchronization.files.message


data class PermitSynchronizeFileRequest(val fileId: String) {

    private constructor() : this("") // for Jackson

}