package net.dankito.service.data

import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class LocalFileInfoService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier)
    : EntityServiceBase<LocalFileInfo>(LocalFileInfo::class.java, dataManager, entityChangedNotifier)
