package net.dankito.deepthought.data

import net.dankito.deepthought.model.FileLink
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import java.io.File
import java.util.*


class FileManager(private val platformConfiguration: IPlatformConfiguration, private val hashService: HashService) {

    fun createLocalFile(localFile: File): FileLink {
        val relativePath = localFile.toRelativeString(platformConfiguration.getApplicationFolder())
        val file = FileLink(relativePath, localFile.name, true, localFile.isDirectory)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())
        file.hashSHA512 = hashService.getFileHash(HashAlgorithm.SHA512, localFile)

        return file
    }

    fun getLocalPathForFile(file: FileLink): File {
        // TODO: read from LocalFileInfo
        return File(platformConfiguration.getApplicationFolder(), file.uriString)
    }

}