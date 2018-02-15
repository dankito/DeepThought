package net.dankito.deepthought.files

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import java.io.File
import java.net.URI


class MimeTypeService(val detector: MimeTypeDetector, val categorizer: MimeTypeCategorizer, private val dataManager: DataManager) {

    fun getBestMimeType(uri: URI): String? {
        return detector.getBestPickForUri(uri)
    }

    fun getBestMimeType(file: File): String? {
        return detector.getBestPickForFile(file)
    }

    fun getBestMimeType(filename: String): String? {
        return detector.getBestPickForFilename(filename)
    }


    fun isHttpUrlAWebPage(httpUrl: String): Boolean {
        val mimeType = detector.getBestPickForFilename(httpUrl)

        return mimeType == null || categorizer.isHtmlFile(mimeType) // if it's null then it's a least not a known mime type, e.g. not a Pdf, image etc., but foreseeable a web page without file extension
    }


    fun getFileType(uri: URI): FileType? {
        return getFileTypeForMimeType(getBestMimeType(uri))
    }

    fun getFileType(file: File): FileType? {
        return getFileTypeForMimeType(getBestMimeType(file))
    }

    fun getFileType(filename: String): FileType? {
        return getFileTypeForMimeType(getBestMimeType(filename))
    }

    fun getFileType(file: FileLink): FileType? {
        return getFileTypeForMimeType(file.mimeType)
    }

    fun getFileTypeForMimeType(mimeType: String?): FileType? {
        if(mimeType == null) {
            return getOtherFilesFileType()
        }

        return when {
            categorizer.isDocument(mimeType) -> getFileTypeByKey("document")
            categorizer.isImageFile(mimeType) -> getFileTypeByKey("image")
            categorizer.isAudioFile(mimeType) -> getFileTypeByKey("audio")
            categorizer.isVideoFile(mimeType) -> getFileTypeByKey("video")
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