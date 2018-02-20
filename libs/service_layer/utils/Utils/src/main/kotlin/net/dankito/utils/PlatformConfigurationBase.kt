package net.dankito.utils

import net.dankito.deepthought.model.enums.FileType
import java.io.File


abstract class PlatformConfigurationBase : IPlatformConfiguration {

    companion object {
        const val DataFolderName = "data"

        const val FilesFolderName = "files"
    }


    override fun getDefaultSavePathForFile(filename: String, fileType: FileType?): File {
        var fileFolder = getDefaultFilesFolder()

        fileType?.let {
            fileFolder = File(fileFolder, fileType.folderName)
        }

        val destinationFile = File(fileFolder, filename)
        return destinationFile // use path not absolute path as for Java synchronized files get stored relative to DeepThought.jar -> if DeepThought.jar and files folder get moved, file still gets found
    }

    protected fun ensureFolderExists(folder: File): File {
        if(folder.exists() == false) {
            folder.mkdirs()
        }

        return folder
    }

}