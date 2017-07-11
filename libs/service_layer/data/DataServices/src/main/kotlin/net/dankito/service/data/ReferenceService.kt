package net.dankito.service.data

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class ReferenceService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Reference>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<Reference> {
        return Reference::class.java
    }

}