package net.dankito.service.data

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.extensions.entryPreview
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class EntryService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): EntityServiceBase<Entry>(Entry::class.java, dataManager, entityChangedNotifier) {

    override fun onPrePersist(entity: Entry) {
        super.onPrePersist(entity)

        dataManager.deepThought.let { deepThought ->
            entity.entryIndex = deepThought.increaseNextEntryIndex()
            dataManager.entityManager.updateEntity(deepThought) // update DeepThought in Db as otherwise new nextEntryIndex doesn't get saved
        }

        updatePreview(entity)
    }

    override fun update(entity: Entry, didChangesAffectingDependentEntities: Boolean) {
        updatePreview(entity)

        super.update(entity, didChangesAffectingDependentEntities)
    }

    private fun updatePreview(entity: Entry) {
        entity.preview = entity.entryPreview
    }

}
