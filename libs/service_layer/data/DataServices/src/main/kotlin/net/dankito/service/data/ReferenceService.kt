package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.eventbus.IEventBus


class ReferenceService(dataManager: DataManager, eventBus: IEventBus) : EntityServiceBase<Reference>(dataManager, eventBus) {

    override fun getEntityClass(): Class<Reference> {
        return Reference::class.java
    }

    override fun addEntityToDeepThought(deepThought: DeepThought, entity: Reference) {
        deepThought.addReference(entity)
    }

    override fun createEntityChangedMessage(entity: Reference, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ReferenceChanged(entity, changeType)
    }

}