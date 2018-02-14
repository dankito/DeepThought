package net.dankito.deepthought.files

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import java.io.File


class MimeTypeService(private val mimeTypeDetector: MimeTypeDetector, private val mimeTypeCategorizer: MimeTypeCategorizer,
                      private val dataManager: DataManager) {

    fun getBestMimeType(file: File): String? {
        return mimeTypeDetector.getBestPickForFile(file)
    }


    fun getFileTypeForMimeType(file: FileLink): FileType? {
        return getFileTypeForMimeType(file.mimeType)
    }

    fun getFileTypeForMimeType(mimeType: String?): FileType? {
        if(mimeType == null) {
            return getOtherFilesFileType()
        }

        return when {
            mimeTypeCategorizer.isDocument(mimeType) -> getFileTypeByKey("document")
            mimeTypeCategorizer.isImageFile(mimeType) -> getFileTypeByKey("image")
            mimeTypeCategorizer.isAudioFile(mimeType) -> getFileTypeByKey("audio")
            mimeTypeCategorizer.isVideoFile(mimeType) -> getFileTypeByKey("video")
            else -> getOtherFilesFileType()
        }
    }

    private fun getOtherFilesFileType(): FileType? {
        return getFileTypeByKey("other.files")
    }

    private fun getFileTypeByKey(key: String): FileType? {
        val fullKey = "file.type." + key

        dataManager.deepThought.fileTypes.forEach { fileType ->
            if(fileType.nameResourceKey == fullKey) {
                return fileType
            }
        }

        return null
    }

}