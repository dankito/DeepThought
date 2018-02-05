package net.dankito.service.data

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class FileService(private val localFileInfoService: LocalFileInfoService, dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier)
    : EntityServiceBase<FileLink>(FileLink::class.java, dataManager, entityChangedNotifier) {


    override fun delete(entity: FileLink) {
        entity.localFileInfo?.let { localFileInfo ->
            localFileInfoService.delete(localFileInfo)

            entity.localFileInfo = null
        }

        super.delete(entity)
    }

}
