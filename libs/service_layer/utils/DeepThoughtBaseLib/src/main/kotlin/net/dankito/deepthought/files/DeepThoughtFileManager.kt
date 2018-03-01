package net.dankito.deepthought.files

import net.dankito.deepthought.files.synchronization.FileSyncService
import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.service.eventbus.IEventBus
import net.dankito.synchronization.files.MimeTypeService
import net.dankito.synchronization.files.persistence.ILocalFileInfoRepository
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.util.IThreadPool
import net.dankito.util.hashing.HashService
import java.io.File


class DeepThoughtFileManager(searchEngine: ISearchEngine<FileLink>, localFileInfoRepository: ILocalFileInfoRepository, fileSyncService: FileSyncService,
                             mimeTypeService: MimeTypeService, hashService: HashService, eventBus: IEventBus, threadPool: IThreadPool)
    : FileManager(searchEngine, localFileInfoRepository, fileSyncService, mimeTypeService, hashService, eventBus, threadPool) {

    fun createLocalDeepThoughtFile(localFile: File, mimeType: String? = null): DeepThoughtFileLink {
        return createLocalFile(localFile, mimeType) as DeepThoughtFileLink
    }

    fun createDownloadedLocalDeepThoughtFile(url: String, localFile: File, mimeType: String? = null): DeepThoughtFileLink {
        return createDownloadedLocalFile(url, localFile, mimeType) as DeepThoughtFileLink
    }


    override fun createFileInstance(uriString: String, name: String, isLocalFile: Boolean): FileLink {
        return DeepThoughtFileLink(uriString, name, isLocalFile)
    }

}