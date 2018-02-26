package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.util.localization.Localization


class AllItemsCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization)
    : CalculatedTag(localization.getLocalizedString("calculated.tag.all.items"), searchEngine, eventBus, entityChangedNotifier) {

    override fun retrieveItemsAsync(done: (List<Item>) -> Unit) {
        searchEngine.searchItems(ItemsSearch {
            done(it)
        })
    }

}