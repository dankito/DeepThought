package net.dankito.service.data

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.service.data.DataManager


class ReferenceService(dataManager: DataManager) : EntityServiceBase<Reference>(dataManager) {

    override fun getEntityClass(): Class<Reference> {
        return Reference::class.java
    }

}