package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.engio.mbassy.listener.Handler


abstract class CalculatedTag(name: String, protected val searchEngine: ISearchEngine, protected val eventBus: IEventBus, protected val entityChangedNotifier: EntityChangedNotifier)
    : Tag(name) {

    companion object {
        const val CountCalculatedTags = 2
    }


    private val eventBusListener = EventBusListener()


    init {
        searchEngine.addInitializationListener { retrieveAndUpdateEntriesAsync(true) }

        eventBus.register(eventBusListener)
    }


    private fun retrieveAndUpdateEntriesAsync(informUIOfUpdate: Boolean) {
        retrieveEntriesAsync {
            this.entries = it

            if(informUIOfUpdate) {
                entityChangedNotifier.notifyListenersOfEntityChangeAsync(this, EntityChangeType.Updated, EntityChangeSource.Local)
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