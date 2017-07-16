package net.dankito.deepthought.model

import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch


class AllEntriesCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus) : CalculatedTag("All Entries", searchEngine, eventBus) { // TODO: translate

    override fun retrieveEntriesAsync(done: (List<Entry>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch {
            done(it)
        })
    }

}