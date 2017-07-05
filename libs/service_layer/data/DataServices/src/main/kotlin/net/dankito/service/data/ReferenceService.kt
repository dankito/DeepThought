package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.ReferenceChanged


class ReferenceService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Reference>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<Reference> {
        return Reference::class.java
    }

    override fun createEntityChangedMessage(entity: Reference, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ReferenceChanged(entity, changeType)
    }

}