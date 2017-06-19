package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus


class EntryService(dataManager: DataManager, eventBus: IEventBus): EntityServiceBase<Entry>(dataManager, eventBus) {

    override fun getEntityClass(): Class<Entry> {
        return Entry::class.java
    }

    override fun addEntityToDeepThought(deepThought: DeepThought, entity: Entry) {
        deepThought.addEntry(entity)
    }

    override fun createEntityChangedMessage(entity: Entry, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return EntryChanged(entity, changeType)
    }

}
