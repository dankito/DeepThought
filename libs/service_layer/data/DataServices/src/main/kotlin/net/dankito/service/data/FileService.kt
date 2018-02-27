package net.dankito.service.data

import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.deepthought.model.DeepThoughtFileLink


class FileService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier)
    : EntityServiceBase<DeepThoughtFileLink>(DeepThoughtFileLink::class.java, dataManager, entityChangedNotifier)
