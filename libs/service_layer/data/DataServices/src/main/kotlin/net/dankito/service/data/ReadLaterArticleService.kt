package net.dankito.service.data

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class ReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<ReadLaterArticle>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<ReadLaterArticle> {
        return ReadLaterArticle::class.java
    }

}