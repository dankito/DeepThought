package net.dankito.synchronization.files

import net.dankito.synchronization.files.persistence.ILocalFileInfoRepository
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.synchronization.model.enums.FileSyncStatus
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.synchronization.search.specific.LocalFileInfoSearch
import net.dankito.synchronization.service.MimeTypeService
import net.dankito.util.IThreadPool
import net.dankito.util.hashing.HashService
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.schedule


open class FileManager(protected val searchEngine: ISearchEngine<FileLink>, protected val localFileInfoRepository: ILocalFileInfoRepository, protected val fileSyncService: FileSyncService,
                       protected val mimeTypeService: MimeTypeService, protected val hashService: HashService, protected val threadPool: IThreadPool,
                       delayBeforeSearchingForNotSynchronizedFilesSeconds: Int) {

    companion object {
        private val log = LoggerFactory.getLogger(FileManager::class.java)
    }


    protected val localFileInfoCache = ConcurrentHashMap<FileLink, LocalFileInfo>()


    init {
        Timer().schedule(delayBeforeSearchingForNotSynchronizedFilesSeconds * 1000L) {
            searchForNotSynchronizedFiles()
        }
    }


    open fun saveLocalFileInfoForLocalFile(file: FileLink) {
        var localFileInfo = getStoredLocalFileInfo(file)

        if(localFileInfo == null) { // should actually never be the case
            localFileInfo = createLocalFileInfoForLocalFile(file, File(file.uriString))
        }

        if(localFileInfo.isPersisted() == false) {
            localFileInfoRepository.persist(localFileInfo)
        }
    }

    open fun createLocalFile(localFile: File, mimeType: String? = null): FileLink {
        val file = createFileInstance(localFile.absolutePath, localFile.name, true)

        file.fileSize = localFile.length()
        file.fileLastModified = Date(localFile.lastModified())

        file.mimeType = mimeType ?: mimeTypeService.getBestMimeType(localFile)
        file.fileType = mimeTypeService.getFileType(file)

        createLocalFileInfoForLocalFile(file, localFile)

        return file
    }

    protected open fun createFileInstance(uriString: String, name: String, isLocalFile: Boolean): FileLink {
        return FileLink(uriString, name, isLocalFile)
    }

    protected open fun createLocalFileInfoForLocalFile(file: FileLink, localFile: File): LocalFileInfo {
        val localFileInfo = LocalFileInfo(file, localFile.absolutePath, true, FileSyncStatus.UpToDate, file.fileSize, file.fileLastModified, file.hashSHA256)
        localFileInfoCache.put(file, localFileInfo)

        setFileHashAsync(file, localFileInfo, localFile) // for large files this takes some time, don't interrupt main routine for calculating hash that long

        return localFileInfo
    }

    open fun createDownloadedLocalFile(url: String, localFile: File, mimeType: String? = null): FileLink {
        val file = createLocalFile(localFile, mimeType)

        file.sourceUriString = url

        return file
    }

    open fun getLocalPathForFile(file: FileLink): File? {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            localFileInfo.path?.let { return File(it) }
        }

        return null
    }

    protected fun setFileHashAsync(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        threadPool.runAsync {
            setFileHash(file, localFileInfo, localFile)
        }
    }

    protected open fun setFileHash(file: FileLink, localFileInfo: LocalFileInfo, localFile: File) {
        try {
            file.hashSHA256 = hashService.getFileHash(FileSyncConfig.FileHashAlgorithm, localFile)

            localFileInfo.hashSHA256 = file.hashSHA256
        } catch(e: Exception) {
            log.error("Could not create file hash for file $file", e)
        }
    }


    open fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(files: Collection<FileLink>) {
        files.forEach { file ->
            forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file)
        }
    }

    open fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(file: FileLink) {
        if(file.isLocalFile) {
            ensureLocalFileInfoIsSet(file)

            checkIfFileSynchronizationShouldGetStarted(file)
        }
    }

    protected open fun ensureLocalFileInfoIsSet(file: FileLink) {
        val storedLocalFileInfo = getStoredLocalFileInfo(file)

        if(storedLocalFileInfo == null) {
            val localFileInfo = LocalFileInfo(file)
            localFileInfoCache.put(file, localFileInfo)

            localFileInfoRepository.persist(localFileInfo)
        }
    }

    open fun getStoredLocalFileInfo(file: FileLink): LocalFileInfo? {
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


    protected open fun fileHasBeenDeleted(file: FileLink) {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            if(localFileInfo.isPersisted()) {
                localFileInfoRepository.delete(localFileInfo)
            }
        }

        localFileInfoCache.remove(file)
    }


    protected open fun checkIfFileSynchronizationShouldGetStarted(file: FileLink) {
        getStoredLocalFileInfo(file)?.let { localFileInfo ->
            // TODO: when updating file has been implemented, also check if hash (and lastModified) still match
            if(localFileInfo.syncStatus != FileSyncStatus.UpToDate) {
                startFileSynchronizationAsync(file)
            }
        }
    }

    protected open fun startFileSynchronizationAsync(file: FileLink) {
        fileSyncService.addFileToSynchronize(file)
    }


    protected open fun searchForNotSynchronizedFiles() {
        searchEngine.searchLocalFileInfo(LocalFileInfoSearch(doesNotHaveSyncStatus = FileSyncStatus.UpToDate) { notUpToDateLocalFileInfo ->
            notUpToDateLocalFileInfo.filterNotNull().sortedBy { it.file?.fileSize ?: Long.MAX_VALUE }.forEach { localFileInfo -> // sort files by their size so that smaller files get synchronized first
                (localFileInfo.file as? FileLink)?.let { file ->
                    startFileSynchronizationAsync(file)
                }
            }

            searchForLocalFilesWithoutLocalFileInfoSet()
        })
    }

    protected open fun searchForLocalFilesWithoutLocalFileInfoSet() {
        // TODO
    }

}