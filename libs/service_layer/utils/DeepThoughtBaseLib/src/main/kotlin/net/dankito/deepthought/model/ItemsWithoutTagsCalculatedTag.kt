package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.utils.localization.Localization


class ItemsWithoutTagsCalculatedTag(searchEngine: ISearchEngine, eventBus: IEventBus, entityChangedNotifier: EntityChangedNotifier, localization: Localization)
    : CalculatedTag(localization.getLocalizedString("calculated.tag.items.with.no.tags"), searchEngine, eventBus, entityChangedNotifier) {

    override fun retrieveItemsAsync(done: (List<Item>) -> Unit) {
        searchEngine.searchItems(ItemsSearch(searchOnlyItemsWithoutTags = true) {
            done(it)
        })
    }

}