package net.dankito.deepthought.files

import net.dankito.deepthought.files.synchronization.FileSyncService
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.data.LocalFileInfoService
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import net.engio.mbassy.listener.Handler
import java.io.File
import java.util.*


class FileManager(private val searchEngine: ISearchEngine, private val localFileInfoService: LocalFileInfoService, private val fileSyncService: FileSyncService,
                  private val platformConfiguration: IPlatformConfiguration, private val hashService: HashService, eventBus: IEventBus, private val threadPool: IThreadPool) {


    private val eventBusListener = EventBusListener()

    init {
        eventBus.register(eventBusListener)
    }


    fun createLocalFile(localFile: File): FileLink {
        val relativePath = localFile.toRelativeString(platformConfiguration.getApplicationFolder())
        val file = FileLink(relativePath, localFile.name, true)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())

        val localFileInfo = LocalFileInfo(file, localFile.absolutePath, true, FileSyncStatus.UpToDate, file.fileSize, file.fileLastModified, file.hashSHA512)
        file.localFileInfo = localFileInfo

        setFileHashAsync(file, localFile) // for large files this takes some time, don't interrupt main routine for calculating hash that long

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

    private fun setFileHashAsync(file: FileLink, localFile: File) {
        threadPool.runAsync {
            setFileHash(file, localFile)
        }
    }

    private fun setFileHash(file: FileLink, localFile: File) {
        file.hashSHA512 = hashService.getFileHash(HashAlgorithm.SHA512, localFile)

        file.localFileInfo?.hashSHA512 = file.hashSHA512
    }


    fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(files: Collection<FileLink>) {
        files.forEach { file ->
            forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file)
        }
    }

    fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file: FileLink) {
        if(file.localFileInfo == null) {
            val storedLocalFileInfo = getStoredLocalFileInfo(file)

            if(storedLocalFileInfo != null) {
                file.localFileInfo = storedLocalFileInfo
            }
            else { // then it's for sure a remote file
                val localFileInfo = LocalFileInfo(file)

                localFileInfoService.persist(localFileInfo)

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
            localFileInfoService.delete(it)
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
        fileSyncService.addFileToSynchronize(file)
    }


    inner class EventBusListener {

        @Handler()
        fun fileChanged(fileChanged: FileChanged) {
            if(fileChanged.changeType == EntityChangeType.PreDelete || fileChanged.changeType == EntityChangeType.Deleted || fileChanged.entity.deleted) {
                deleteLocalFileInfo(fileChanged.entity)
            }
            else {
                forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(fileChanged.entity)
            }
        }

    }

}