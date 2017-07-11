package net.dankito.service.data

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class EntryService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): EntityServiceBase<Entry>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<Entry> {
        return Entry::class.java
    }

    override fun onPrePersist(entity: Entry) {
        super.onPrePersist(entity)

        dataManager.deepThought.let { deepThought ->
            entity.entryIndex = deepThought.increaseNextEntryIndex()
            dataManager.entityManager.updateEntity(deepThought) // update DeepThought in Db as otherwise new nextEntryIndex doesn't get saved
        }
    }

}
