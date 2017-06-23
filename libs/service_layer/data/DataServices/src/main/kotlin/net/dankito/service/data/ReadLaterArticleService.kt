package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.service.eventbus.IEventBus


class ReadLaterArticleService(dataManager: DataManager, eventBus: IEventBus) : EntityServiceBase<ReadLaterArticle>(dataManager, eventBus) {

    override fun getEntityClass(): Class<ReadLaterArticle> {
        return ReadLaterArticle::class.java
    }

    override fun addEntityToDeepThought(deepThought: DeepThought, entity: ReadLaterArticle): Boolean {
        return deepThought.addReadLaterArticle(entity)
    }

    override fun createEntityChangedMessage(entity: ReadLaterArticle, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ReadLaterArticleChanged(entity, changeType)
    }

}