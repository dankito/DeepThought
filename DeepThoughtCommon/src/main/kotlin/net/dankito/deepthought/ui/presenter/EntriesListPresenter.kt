package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class EntriesListPresenter(private val entriesListView: IEntriesListView, private var router: IRouter, private var searchEngine: ISearchEngine)
    : IMainViewSectionPresenter {

    private var lastSearchTermProperty = Search.EmptySearchTerm


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)

            // EntriesListPresenter is the big exception. As it's first displayed at app start no lazy data retrieval is needed for it, get all entries as soon as SearchEngine is initialized
            searchEngine.addInitializationListener {
                retrieveAndShowEntries()
            }
        }
    }

    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    fun showEntry(entry: Entry) {
        router.showViewEntryView(entry)
    }


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener { retrieveAndShowEntries() }
    }

    private fun retrieveAndShowEntries() {
        searchEntries(Search.EmptySearchTerm)
    }


    fun searchEntries(searchTerm: String, searchInContent: Boolean = true, searchInAbstract: Boolean = true) {
       lastSearchTermProperty = searchTerm

        searchEngine.searchEntries(EntriesSearch(searchTerm, searchInContent, searchInAbstract) { result ->
            entriesListView.showEntries(result)
        })
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            retrieveAndShowEntries()
        }

    }

}