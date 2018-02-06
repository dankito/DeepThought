package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.ISourcesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.SourceSearch
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class SourcesListPresenter(private var view: ISourcesListView, private val searchEngine: ISearchEngine, router: IRouter,
                           clipboardService: IClipboardService, deleteEntityService: DeleteEntityService)
    : SourcePresenterBase(router, clipboardService, deleteEntityService), IMainViewSectionPresenter {


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    private var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastSourceSearch: SourceSearch? = null


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)
        }
    }


    private fun retrieveAndShowSourcess() {
        searchSources(lastSearchTermProperty)
    }


    fun searchSources(searchTerm: String, searchCompleted: ((List<Source>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        lastSourceSearch?.interrupt()

        lastSourceSearch = SourceSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        }

        lastSourceSearch?.let { searchEngine.searchSources(it) }
    }

    private fun retrievedSearchResults(result: List<Source>) {
        view.showEntities(result)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }



    fun showItemsForSource(source: Source) {
        router.showItemsForSource(source)
    }


    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entityChanged: EntitiesOfTypeChanged) {
            if(entityChanged.entityType == Source::class.java) {
                retrieveAndShowSourcess()
            }
        }

    }

}