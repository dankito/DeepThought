package net.dankito.deepthought.files.synchronization


class FileSyncConfig {

    companion object {
        const val BufferSize = 8 * 1024

        val BeginToStreamFileMessage = "BEGIN".toByteArray()
    }

}