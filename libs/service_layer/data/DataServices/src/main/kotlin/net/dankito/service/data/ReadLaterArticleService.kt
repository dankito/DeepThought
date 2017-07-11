package net.dankito.service.data

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class ReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<ReadLaterArticle>(ReadLaterArticle::class.java, dataManager, entityChangedNotifier)