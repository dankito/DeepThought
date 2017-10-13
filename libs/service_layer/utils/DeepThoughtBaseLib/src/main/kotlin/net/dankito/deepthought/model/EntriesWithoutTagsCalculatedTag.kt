package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch
import net.dankito.utils.localization.Localization


class EntriesWithoutTagsCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization)
    : CalculatedTag(localization.getLocalizedString("calculated.tag.entries.with.no.tags"), searchEngine, eventBus, entityChangedNotifier) {

    override fun retrieveEntriesAsync(done: (List<Item>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch(filterOnlyEntriesWithoutTags = true) {
            done(it)
        })
    }

}