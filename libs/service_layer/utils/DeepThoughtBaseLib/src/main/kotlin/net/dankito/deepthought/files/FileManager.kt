package net.dankito.deepthought.files

import net.dankito.deepthought.files.synchronization.FileSyncConfig
import net.dankito.deepthought.files.synchronization.FileSyncService
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.data.LocalFileInfoService
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.LocalFileInfoSearch
import net.dankito.util.IThreadPool
import net.dankito.utils.services.Times
import net.dankito.util.hashing.HashService
import net.engio.mbassy.listener.Handler
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.schedule


class FileManager(private val searchEngine: ISearchEngine, private val localFileInfoService: LocalFileInfoService, private val fileSyncService: FileSyncService,
                  private val mimeTypeService: MimeTypeService, private val hashService: HashService, eventBus: IEventBus, private val threadPool: IThreadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(FileManager::class.java)
    }


    private val localFileInfoCache = ConcurrentHashMap<FileLink, LocalFileInfo>()

    private val eventBusListener = EventBusListener()


    init {
        eventBus.register(eventBusListener)

        Timer().schedule(Times.DefaultDelayBeforeSearchingForNotSynchronizedFilesSeconds * 1000L) {
            searchForNotSynchronizedFiles()
        }
    }


    fun saveLocalFileInfoForLocalFile(file: FileLink) {
        var localFileInfo = getStoredLocalFileInfo(file)

        if(localFileInfo == null) { // should actually never be the case
            localFileInfo = createLocalFileInfoForLocalFile(file, File(file.uriString))
        }

        if(localFileInfo.isPersisted() == false) {
            localFileInfoService.persist(localFileInfo)
        }
    }

    fun createLocalFile(localFile: File, mimeType: String? = null): FileLink {
        val file = FileLink(localFile.absolutePath, localFile.name, true)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())

        file.mimeType = mimeType ?: mimeTypeService.getBestMimeType(localFile)
        file.fileType = mimeTypeService.getFileType(file)

        createLocalFileInfoForLocalFile(file, localFile)

        return file
    }

    private fun createLocalFileInfoForLocalFile(file: FileLink, localFile: File): LocalFileInfo {
        val localFileInfo = LocalFileInfo(file, localFile.absolutePath, true, FileSyncStatus.UpToDate, file.fileSize, file.fileLastModified, file.hashSHA256)
        localFileInfoCache.put(file, localFileInfo)

        setFileHashAsync(file, localFileInfo, localFile) // for large files this takes some time, don't interrupt main routine for calculating hash that long

        return localFileInfo
    }

    fun createDownloadedLocalFile(url: String, localFile: File, mimeType: String? = null): FileLink {
        val file = createLocalFile(localFile, mimeType)

        file.sourceUriString = url

        return file
    }

    fun getLocalPathForFile(file: FileLink): File? {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            localFileInfo.path?.let { return File(it) }
        }

        return null
    }

    private fun setFileHashAsync(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        threadPool.runAsync {
            setFileHash(file, localFileInfo, localFile)
        }
    }

    private fun setFileHash(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        try {
            file.hashSHA256 = hashService.getFileHash(FileSyncConfig.FileHashAlgorithm, localFile)

            localFileInfo.hashSHA256 = file.hashSHA256
        } catch(e: Exception) {
            log.error("Could not create file hash for file $file", e)
        }
    }


    fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(files: Collection<FileLink>) {
        files.forEach { file ->
            forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file)
        }
    }

    fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file: FileLink) {
        if(file.isLocalFile) {
            ensureLocalFileInfoIsSet(file)

            checkIfFileSynchronizationShouldGetStarted(file)
        }
    }

    private fun ensureLocalFileInfoIsSet(file: FileLink) {
        val storedLocalFileInfo = getStoredLocalFileInfo(file)

        if(storedLocalFileInfo == null) {
            val localFileInfo = LocalFileInfo(file)
            localFileInfoCache.put(file, localFileInfo)

            localFileInfoService.persist(localFileInfo)
        }
    }

    fun getStoredLocalFileInfo(file: FileLink): LocalFileInfo? {
        localFileInfoCache.get(file)?.let { return it }

        val localFileInfo = AtomicReference<LocalFileInfo?>(null)

        searchEngine.searchLocalFileInfo(LocalFileInfoSearch(file.id) { result ->
            if(result.isNotEmpty()) {
                localFileInfoCache.put(file, result[0])
                localFileInfo.set(result[0])
            }
        })

        return localFileInfo.get()
    }


    private fun fileHasBeenDeleted(file: FileLink) {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            if(localFileInfo.isPersisted()) {
                localFileInfoService.delete(localFileInfo)
            }
        }

        localFileInfoCache.remove(file)
    }


    private fun checkIfFileSynchronizationShouldGetStarted(file: FileLink) {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            // TODO: when updating file has been implemented, also check if hash (and lastModified) still match
            if(localFileInfo.syncStatus != FileSyncStatus.UpToDate) {
                startFileSynchronizationAsync(file)
            }
        }
    }

    private fun startFileSynchronizationAsync(file: FileLink) {
        fileSyncService.addFileToSynchronize(file)
    }


    private fun searchForNotSynchronizedFiles() {
        searchEngine.searchLocalFileInfo(LocalFileInfoSearch(doesNotHaveSyncStatus = FileSyncStatus.UpToDate) { notUpToDateLocalFileInfo ->
            notUpToDateLocalFileInfo.filterNotNull().sortedBy { it.file?.fileSize ?: Long.MAX_VALUE }.forEach { localFileInfo -> // sort files by their size so that smaller files get synchronized first
                localFileInfo.file?.let { file ->
                    startFileSynchronizationAsync(file)
                }
            }

            searchForLocalFilesWithoutLocalFileInfoSet()
        })
    }

    private fun searchForLocalFilesWithoutLocalFileInfoSet() {
        // TODO
    }


    inner class EventBusListener {

        @Handler()
        fun fileChanged(fileChanged: FileChanged) {
            if(fileChanged.changeType == EntityChangeType.PreDelete || fileChanged.changeType == EntityChangeType.Deleted || fileChanged.entity.deleted) {
                fileHasBeenDeleted(fileChanged.entity)
            }
            else {
                forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(fileChanged.entity)
            }
        }

    }

}