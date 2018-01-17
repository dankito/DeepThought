package net.dankito.deepthought.data

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import java.io.File
import java.util.*


class FileManager(private val searchEngine: ISearchEngine, private val platformConfiguration: IPlatformConfiguration, private val hashService: HashService) {

    fun createLocalFile(localFile: File): FileLink {
        val relativePath = localFile.toRelativeString(platformConfiguration.getApplicationFolder())
        val file = FileLink(relativePath, localFile.name, true)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())
        file.hashSHA512 = hashService.getFileHash(HashAlgorithm.SHA512, localFile)

        val localFileInfo = LocalFileInfo(file, localFile.absolutePath, true, FileSyncStatus.UpToDate, file.fileSize, file.fileLastModified, file.hashSHA512)
        file.localFileInfo = localFileInfo

        return file
    }

    fun getLocalPathForFile(file: FileLink): File {
        file.localFileInfo?.path?.let { return File(it) } // LocalFileInfo already set

        getLocalFileInfo(file)?.let { localFileInfo -> // Retrieve LocalFileInfo
            file.localFileInfo = localFileInfo

            localFileInfo.path?.let { return File(it) }
        }

        return File(platformConfiguration.getApplicationFolder(), file.uriString)
    }


    fun ensureLocalFileInfoIsSet(files: Collection<FileLink>) {
        files.forEach { file ->
            if(file.localFileInfo == null) {
                setLocalFileInfo(file)
            }
        }
    }

    fun setLocalFileInfo(file: FileLink) {
        file.localFileInfo = getLocalFileInfo(file)
    }

    private fun getLocalFileInfo(file: FileLink): LocalFileInfo? {
        return searchEngine.getLocalFileInfo(file)
    }

}