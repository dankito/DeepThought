package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.localization.Localization


class AllCalculatedTags(private val searchEngine: ISearchEngine, private val eventBus: IEventBus, private val entityChangedNotifier: EntityChangedNotifier,
                        private val localization: Localization) {

    private var hasInitializedCalculatedTags = false

    private val calculatedTags = ArrayList<CalculatedTag>()


    fun getCalculatedTags(): List<Tag> {
        if(hasInitializedCalculatedTags == false) {
            initCalculatedTags()
        }

        return calculatedTags
    }

    private fun initCalculatedTags() {
        calculatedTags.add(AllEntriesCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))
        calculatedTags.add(EntriesWithoutTagsCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))

        hasInitializedCalculatedTags = true
    }
}