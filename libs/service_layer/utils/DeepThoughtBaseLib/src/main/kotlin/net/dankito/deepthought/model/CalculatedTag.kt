package net.dankito.deepthought.model

import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.ItemChanged
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
        searchEngine.addInitializationListener { retrieveAndUpdateItemsAsync(true) }

        eventBus.register(eventBusListener)
    }


    private fun retrieveAndUpdateItemsAsync(informUIOfUpdate: Boolean) {
        retrieveEntriesAsync {
            this.items = it

            if(informUIOfUpdate) {
                entityChangedNotifier.notifyListenersOfEntityChangeAsync(this, EntityChangeType.Updated, EntityChangeSource.Local)
            }
        }
    }

    protected abstract fun retrieveEntriesAsync(done: (List<Item>) -> Unit)


    inner class EventBusListener {

        @Handler()
        fun itemChanged(itemChanged: ItemChanged) {
            retrieveAndUpdateItemsAsync(true)
        }

    }
}