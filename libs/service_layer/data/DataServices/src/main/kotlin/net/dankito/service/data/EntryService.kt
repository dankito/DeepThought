package net.dankito.service.data

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.service.data.DataManager


class EntryService(dataManager: DataManager): EntityServiceBase<Entry>(dataManager) {

    override fun getEntityClass(): Class<Entry> {
        return Entry::class.java
    }

}
