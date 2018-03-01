package net.dankito.util.filesystem

import android.content.Context
import java.io.File


class AndroidFileStorageService(val context: Context) : JavaFileStorageService() {


    override fun getFileInDataFolder(filename: String, folderName: String?): File {
        return ensureFolderInDataFolderExists(context.filesDir, filename, folderName)
    }

}