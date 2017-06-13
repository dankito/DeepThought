package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IMainView
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class MainViewPresenter(private val mainView: IMainView, private var router: IRouter, private var dataManager: DataManager, private var searchEngine: ISearchEngine) {

    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    fun clickedOnEntry(entry: Entry) {
        router.showEntryView(entry)
    }


    fun setupDataAsync() {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)

            searchEngine.addInitializationListener {
                searchEngineInitialized()
            }
        }
    }

    fun onDestroy() {
        eventBus.unregister(eventBusListener)
    }

    private fun searchEngineInitialized() {
        retrieveAndShowEntries()
    }

    private fun retrieveAndShowEntries() {
        searchEngine.searchEntries(EntriesSearch { result ->
            mainView.showEntries(result)
        })
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            retrieveAndShowEntries()
        }

    }

}