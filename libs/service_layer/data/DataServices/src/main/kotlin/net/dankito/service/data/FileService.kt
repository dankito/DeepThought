package net.dankito.service.data

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class FileService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<FileLink>(FileLink::class.java, dataManager, entityChangedNotifier) {

    override fun onPostPersist(entity: FileLink) {
        super.onPostPersist(entity)

        entity.localFileInfo?.let { localFileInfo ->
            dataManager.entityManager.persistEntity(localFileInfo)
        }
    }


    override fun delete(entity: FileLink) {
        entity.localFileInfo?.let { localFileInfo ->
            dataManager.entityManager.deleteEntity(localFileInfo)

            entity.localFileInfo = null
        }

        super.delete(entity)
    }

}
