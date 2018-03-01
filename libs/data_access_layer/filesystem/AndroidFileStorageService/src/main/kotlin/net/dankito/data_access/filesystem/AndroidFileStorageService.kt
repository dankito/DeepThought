package net.dankito.data_access.filesystem

import android.content.Context
import net.dankito.util.filesystem.JavaFileStorageService
import java.io.File


class AndroidFileStorageService(val context: Context) : JavaFileStorageService() {


    override fun getFileInDataFolder(filename: String, folderName: String?): File {
        return ensureFolderInDataFolderExists(context.filesDir, filename, folderName)
    }

}