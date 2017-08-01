package net.dankito.data_access.filesystem

import android.content.Context
import java.io.File


class AndroidFileStorageService(val context: Context) : JavaFileStorageService() {


    override fun getFileInDataFolder(filename: String): File {
        return File(context.filesDir, filename)
    }

}