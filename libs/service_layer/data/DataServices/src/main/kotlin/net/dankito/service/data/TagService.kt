package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.TagChanged


class TagService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Tag>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<Tag> {
        return Tag::class.java
    }

    override fun createEntityChangedMessage(entity: Tag, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return TagChanged(entity, changeType)
    }

}