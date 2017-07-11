package net.dankito.service.data

import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class TagService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Tag>(Tag::class.java, dataManager, entityChangedNotifier)