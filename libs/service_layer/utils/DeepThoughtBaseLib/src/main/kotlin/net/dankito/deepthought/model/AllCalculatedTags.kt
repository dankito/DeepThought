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
        synchronized(this) {
            if(hasInitializedCalculatedTags == false) {
                initCalculatedTags()
            }
        }

        return calculatedTags
    }

    private fun initCalculatedTags() {
        calculatedTags.add(AllItemsCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))
        calculatedTags.add(ItemsWithoutTagsCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))

        hasInitializedCalculatedTags = true
    }


    fun getCalculatedTagForName(name: String): Tag? {
        getCalculatedTags().forEach { tag ->
            if(tag.name == name) {
                return tag
            }
        }

        return null
    }

}