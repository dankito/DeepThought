package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.TagChanged
import net.dankito.service.eventbus.IEventBus


class TagService(dataManager: DataManager, eventBus: IEventBus) : EntityServiceBase<Tag>(dataManager, eventBus) {

    override fun getEntityClass(): Class<Tag> {
        return Tag::class.java
    }

    override fun addEntityToDeepThought(deepThought: DeepThought, entity: Tag): Boolean {
        return deepThought.addTag(entity)
    }

    override fun createEntityChangedMessage(entity: Tag, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return TagChanged(entity, changeType)
    }

}