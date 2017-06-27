package net.dankito.deepthought.model

import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch


class EntriesWithoutTagsCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus) : CalculatedTag("Entries without tags", searchEngine, eventBus) { // TODO: translate

    override fun retrieveEntriesAsync(done: (List<Entry>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch(filterOnlyEntriesWithoutTags = true) {
            done(it)
        })
    }

}