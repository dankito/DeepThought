package net.dankito.deepthought.data

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import net.engio.mbassy.listener.Handler
import java.io.File
import java.util.*


class FileManager(private val searchEngine: ISearchEngine, private val platformConfiguration: IPlatformConfiguration, private val hashService: HashService, eventBus: IEventBus) {


    init {
        eventBus.register(EventBusListener())
    }


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

        getStoredLocalFileInfo(file)?.let { localFileInfo -> // Retrieve LocalFileInfo
            file.localFileInfo = localFileInfo

            localFileInfo.path?.let { return File(it) }
        }

        // TODO: return that file doesn't exist locally yet
        return File(platformConfiguration.getApplicationFolder(), file.uriString)
    }


    fun ensureLocalFileInfoIsSetAndMayStartSynchronization(files: Collection<FileLink>) {
        files.forEach { file ->
            ensureLocalFileInfoIsSetAndMayStartSynchronization(file)
        }
    }

    fun ensureLocalFileInfoIsSetAndMayStartSynchronization(file: FileLink) {
        if(file.localFileInfo == null) {
            val storedLocalFileInfo = getStoredLocalFileInfo(file)

            if(storedLocalFileInfo != null) {
                file.localFileInfo = storedLocalFileInfo
            }
            else { // then it's for sure a remote file
                val localFileInfo = LocalFileInfo(file)

                // TODO: save localFileInfo

                file.localFileInfo = localFileInfo
            }
        }

        checkIfFileSynchronizationShouldGetStarted(file)
    }

    private fun getStoredLocalFileInfo(file: FileLink): LocalFileInfo? {
        return searchEngine.getLocalFileInfo(file)
    }


    private fun deleteLocalFileInfo(file: FileLink) {
        file.localFileInfo?.let {
            // TODO: delete
        }
    }


    private fun checkIfFileSynchronizationShouldGetStarted(file: FileLink) {
        file.localFileInfo?.let { localFileInfo ->
            if(localFileInfo.syncStatus != FileSyncStatus.UpToDate) {
                startFileSynchronizationAsync(file)
            }
        }
    }

    private fun startFileSynchronizationAsync(file: FileLink) {
        // TODO
    }


    inner class EventBusListener {

        @Handler()
        fun fileChanged(fileChanged: FileChanged) {
            if(fileChanged.changeType != EntityChangeType.PreDelete && fileChanged.changeType != EntityChangeType.Deleted) {
                deleteLocalFileInfo(fileChanged.entity)
            }
            else {
                ensureLocalFileInfoIsSetAndMayStartSynchronization(fileChanged.entity)
            }
        }

    }

}