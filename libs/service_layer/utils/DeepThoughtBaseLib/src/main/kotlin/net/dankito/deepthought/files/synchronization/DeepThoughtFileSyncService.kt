package net.dankito.deepthought.files.synchronization

import net.dankito.synchronization.device.service.IConnectedDevicesService
import net.dankito.synchronization.files.FileSyncService
import net.dankito.synchronization.files.persistence.ILocalFileInfoRepository
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.synchronization.service.permissions.IPermissionsService
import net.dankito.util.hashing.HashService
import net.dankito.util.network.SocketHandler
import net.dankito.util.serialization.ISerializer
import net.dankito.utils.IPlatformConfiguration
import java.io.File


class DeepThoughtFileSyncService(connectedDevicesService: IConnectedDevicesService, searchEngine: ISearchEngine<FileLink>, socketHandler: SocketHandler,
                                 localFileInfoRepository: ILocalFileInfoRepository, serializer: ISerializer, permissionsService: IPermissionsService,
                                 protected val platformConfiguration: IPlatformConfiguration, hashService: HashService)
    : FileSyncService(connectedDevicesService, searchEngine, socketHandler, localFileInfoRepository, serializer, permissionsService, hashService) {

    override fun getDefaultSavePathForFile(file: FileLink): File {
        return platformConfiguration.getDefaultSavePathForFile(file.name, file.fileType)
    }

}