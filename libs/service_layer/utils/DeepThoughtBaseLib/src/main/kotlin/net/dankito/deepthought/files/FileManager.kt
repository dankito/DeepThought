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
import net.dankito.service.search.specific.LocalFileInfoSearch
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.Times
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import net.engio.mbassy.listener.Handler
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.schedule


class FileManager(private val searchEngine: ISearchEngine, private val localFileInfoService: LocalFileInfoService, private val fileSyncService: FileSyncService,
                  private val platformConfiguration: IPlatformConfiguration, private val hashService: HashService, eventBus: IEventBus, private val threadPool: IThreadPool) {

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


    fun createLocalFile(localFile: File): FileLink {
        val relativePath = localFile.toRelativeString(platformConfiguration.getApplicationFolder())
        val file = FileLink(relativePath, localFile.name, true)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())

        val localFileInfo = LocalFileInfo(file, localFile.absolutePath, true, FileSyncStatus.UpToDate, file.fileSize, file.fileLastModified, file.hashSHA512)
        localFileInfoCache.put(file, localFileInfo)

        setFileHashAsync(file, localFileInfo, localFile) // for large files this takes some time, don't interrupt main routine for calculating hash that long

        return file
    }

    fun getLocalPathForFile(file: FileLink): File {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            localFileInfo.path?.let { return File(it) }
        }

        // TODO: return that file doesn't exist locally yet
        return File(platformConfiguration.getApplicationFolder(), file.uriString)
    }

    private fun setFileHashAsync(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        threadPool.runAsync {
            setFileHash(file, localFileInfo, localFile)
        }
    }

    private fun setFileHash(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        try {
            file.hashSHA512 = hashService.getFileHash(HashAlgorithm.SHA512, localFile)

            localFileInfo.hashSHA512 = file.hashSHA512
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
            notUpToDateLocalFileInfo.filterNotNull().forEach { localFileInfo ->
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