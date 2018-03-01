package net.dankito.deepthought.files

import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.model.enums.FileType
import java.io.File
import java.net.URI


class MimeTypeService(val detector: MimeTypeDetector, val categorizer: MimeTypeCategorizer) {

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


    fun getFileType(uri: URI): FileType {
        return getFileTypeForMimeType(getBestMimeType(uri))
    }

    fun getFileType(file: File): FileType {
        return getFileTypeForMimeType(getBestMimeType(file))
    }

    fun getFileType(filename: String): FileType {
        return getFileTypeForMimeType(getBestMimeType(filename))
    }

    fun getFileType(file: FileLink): FileType {
        return getFileTypeForMimeType(file.mimeType)
    }

    fun getFileTypeForMimeType(mimeType: String?): FileType {
        if(mimeType == null) {
            return FileType.Other
        }

        return when {
            categorizer.isDocument(mimeType) -> FileType.Document
            categorizer.isImageFile(mimeType) -> FileType.Image
            categorizer.isAudioFile(mimeType) -> FileType.Audio
            categorizer.isVideoFile(mimeType) -> FileType.Video
            else -> FileType.Other
        }
    }

}