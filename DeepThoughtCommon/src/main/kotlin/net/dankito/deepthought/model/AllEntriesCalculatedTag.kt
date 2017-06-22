package net.dankito.deepthought.model

import net.dankito.deepthought.service.data.DataManager


class AllEntriesCalculatedTag(private val dataManager: DataManager) : CalculatedTag("All Entries") { // TODO: translate

    init {
        dataManager.addInitializationListener {
            dataManager.currentDeepThought?.let {
                entries = it.entries
            }
        }
    }

}