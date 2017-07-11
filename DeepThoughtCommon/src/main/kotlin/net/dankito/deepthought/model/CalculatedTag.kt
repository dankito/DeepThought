package net.dankito.deepthought.model

import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.engio.mbassy.listener.Handler


abstract class CalculatedTag(name: String, protected val searchEngine: ISearchEngine, protected val eventBus: IEventBus) : Tag(name) {

    private val eventBusListener = EventBusListener()


    init {
        searchEngine.addInitializationListener { retrieveAndUpdateEntriesAsync(false) }

        eventBus.register(eventBusListener)
    }


    private fun retrieveAndUpdateEntriesAsync(informUIOfUpdate: Boolean) {
        retrieveEntriesAsync {
            this.entries = it

            if(informUIOfUpdate) {
                eventBus.postAsync(EntitiesOfTypeChanged(Tag::class.java, EntityChangeType.Updated, EntityChangeSource.Local))
            }
        }
    }

    protected abstract fun retrieveEntriesAsync(done: (List<Entry>) -> Unit)


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entryChanged: EntryChanged) {
            retrieveAndUpdateEntriesAsync(true)
        }

    }
}