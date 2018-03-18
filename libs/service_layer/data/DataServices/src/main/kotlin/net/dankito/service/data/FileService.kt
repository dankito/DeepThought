package net.dankito.service.data

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class FileService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier)
    : EntityServiceBase<FileLink>(FileLink::class.java, dataManager, entityChangedNotifier)
