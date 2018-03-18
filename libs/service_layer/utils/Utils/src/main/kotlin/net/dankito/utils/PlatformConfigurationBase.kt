package net.dankito.utils

import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.model.enums.FileTypeDefaultFolderName
import java.io.File


abstract class PlatformConfigurationBase : IPlatformConfiguration {

    companion object {
        const val DataFolderName = "data"

        const val FilesFolderName = "files"
    }


    override fun getDefaultSavePathForFile(filename: String, fileType: FileType): File {
        val fileFolder = File(getDefaultFilesFolder(), getFolderNameForFileType(fileType))

        return File(fileFolder, filename) // use path not absolute path as for Java synchronized files get stored relative to DeepThought.jar -> if DeepThought.jar and files folder get moved, file still gets found
    }

    private fun getFolderNameForFileType(fileType: FileType): String {
        return when(fileType) {
            FileType.Document -> FileTypeDefaultFolderName.Documents.folderName
            FileType.Image -> FileTypeDefaultFolderName.Images.folderName
            FileType.Audio -> FileTypeDefaultFolderName.Audios.folderName
            FileType.Video -> FileTypeDefaultFolderName.Videos.folderName
            else -> FileTypeDefaultFolderName.OtherFilesFolderName.folderName
        }
    }

    protected fun ensureFolderExists(folder: File): File {
        if(folder.exists() == false) {
            folder.mkdirs()
        }

        return folder
    }

}