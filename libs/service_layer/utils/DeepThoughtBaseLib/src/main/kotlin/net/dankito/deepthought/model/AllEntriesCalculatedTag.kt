package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch
import net.dankito.utils.localization.Localization


class AllEntriesCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization)
    : CalculatedTag(localization.getLocalizedString("calculated.tag.all.items"), searchEngine, eventBus, entityChangedNotifier) {

    override fun retrieveEntriesAsync(done: (List<Item>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch {
            done(it)
        })
    }

}