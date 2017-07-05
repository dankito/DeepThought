package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.ReadLaterArticleChanged


class ReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<ReadLaterArticle>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<ReadLaterArticle> {
        return ReadLaterArticle::class.java
    }

    override fun createEntityChangedMessage(entity: ReadLaterArticle, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ReadLaterArticleChanged(entity, changeType)
    }

}