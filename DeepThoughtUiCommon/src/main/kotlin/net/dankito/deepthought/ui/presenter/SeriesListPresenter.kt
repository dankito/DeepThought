package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.SeriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class SeriesListPresenter(private val view: ISeriesListView, private val searchEngine: ISearchEngine, router: IRouter, deleteEntityService: DeleteEntityService)
    : SeriesPresenterBase(router, deleteEntityService), IMainViewSectionPresenter {


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    private var lastSearchTermProperty = Search.EmptySearchTerm


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)
        }
    }


    fun searchSeries(searchTerm: String = lastSearchTermProperty, searchCompleted: ((List<Series>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchSeries(SeriesSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        })
    }

    private fun retrievedSearchResults(result: List<Series>) {
        view.showEntities(result)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entityChanged: EntitiesOfTypeChanged) {
            if(entityChanged.entityType == Source::class.java) {
                searchSeries()
            }
        }

    }

}