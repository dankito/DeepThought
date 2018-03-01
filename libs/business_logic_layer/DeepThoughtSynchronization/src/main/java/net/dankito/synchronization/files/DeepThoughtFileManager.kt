package net.dankito.synchronization.files

import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.synchronization.files.persistence.ILocalFileInfoRepository
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.util.IThreadPool
import net.dankito.util.event.EntityChangeType
import net.dankito.util.hashing.HashService
import net.dankito.utils.services.Times
import net.engio.mbassy.listener.Handler
import java.io.File


class DeepThoughtFileManager(searchEngine: ISearchEngine<FileLink>, localFileInfoRepository: ILocalFileInfoRepository, fileSyncService: FileSyncService,
                             mimeTypeService: MimeTypeService, hashService: HashService, private val eventBus: IEventBus, threadPool: IThreadPool)
    : FileManager(searchEngine, localFileInfoRepository, fileSyncService, mimeTypeService, hashService, threadPool, Times.DefaultDelayBeforeSearchingForNotSynchronizedFilesSeconds) {

    protected val eventBusListener = EventBusListener()


    init {
        eventBus.register(eventBusListener)
    }


    fun createLocalDeepThoughtFile(localFile: File, mimeType: String? = null): DeepThoughtFileLink {
        return createLocalFile(localFile, mimeType) as DeepThoughtFileLink
    }

    fun createDownloadedLocalDeepThoughtFile(url: String, localFile: File, mimeType: String? = null): DeepThoughtFileLink {
        return createDownloadedLocalFile(url, localFile, mimeType) as DeepThoughtFileLink
    }


    override fun createFileInstance(uriString: String, name: String, isLocalFile: Boolean): FileLink {
        return DeepThoughtFileLink(uriString, name, isLocalFile)
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