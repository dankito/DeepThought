package net.dankito.deepthought.model

import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler


class EntriesWithoutTagsCalculatedTag(private val searchEngine: ISearchEngine, private val eventBus: IEventBus) : CalculatedTag("Entries without tags") { // TODO: translate

    private val eventBusListener = EventBusListener()


    init {
        searchEngine.addInitializationListener { getEntriesWithoutTags() }

        eventBus.register(eventBusListener)
    }


    private fun getEntriesWithoutTags() {
        searchEngine.searchEntries(EntriesSearch(filterOnlyEntriesWithoutTags = true) {
            this.entries = it

            eventBus.postAsync(EntitiesOfTypeChanged(Tag::class.java))
        })
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entryChanged: EntryChanged) {
            getEntriesWithoutTags()
        }

    }

}