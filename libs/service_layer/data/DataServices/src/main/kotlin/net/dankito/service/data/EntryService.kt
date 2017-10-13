package net.dankito.service.data

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.entryPreview
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class EntryService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): EntityServiceBase<Item>(Item::class.java, dataManager, entityChangedNotifier) {

    override fun onPrePersist(entity: Item) {
        super.onPrePersist(entity)

        dataManager.deepThought.let { deepThought ->
            entity.itemIndex = deepThought.increaseNextItemIndex()
            dataManager.entityManager.updateEntity(deepThought) // update DeepThought in Db as otherwise new nextItemIndex doesn't get saved
        }

        updatePreview(entity)
    }

    override fun update(entity: Item, didChangesAffectingDependentEntities: Boolean) {
        updatePreview(entity)

        super.update(entity, didChangesAffectingDependentEntities)
    }

    private fun updatePreview(entity: Item) {
        entity.preview = entity.entryPreview
    }

}
