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

    private var visibleCount = 0


    /*  Actually only needed for Android: If a CalculatedTag is not visible (which there is true in almost all circumstances), don't listen to EventBus to not unnecessarily execute SearchEngine query */

    fun tagBecomesVisible() {
        searchEngine.addInitializationListener { retrieveAndUpdateItemsAsync(true) }

        if(visibleCount == 0) {
            eventBus.register(eventBusListener)
        }

        visibleCount++
    }

    fun tagGetsHidden() {
        if(visibleCount > 0) {
            visibleCount--

            if(visibleCount == 0) {
                eventBus.unregister(eventBusListener)
            }
        }
    }


    private fun retrieveAndUpdateItemsAsync(informUIOfUpdate: Boolean) {
        retrieveItemsAsync {
            this.items = it

            if(informUIOfUpdate) {
                entityChangedNotifier.notifyListenersOfEntityChangeAsync(this, EntityChangeType.Updated, EntityChangeSource.Local)
            }
        }
    }

    protected abstract fun retrieveItemsAsync(done: (List<Item>) -> Unit)


    inner class EventBusListener {

        @Handler()
        fun itemChanged(itemChanged: ItemChanged) {
            retrieveAndUpdateItemsAsync(true)
        }

    }
}