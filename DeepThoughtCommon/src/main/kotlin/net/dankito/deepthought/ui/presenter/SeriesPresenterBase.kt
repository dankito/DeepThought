package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.SeriesChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.SeriesSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


abstract class SeriesPresenterBase(private val seriesListView: ISeriesListView, private val searchEngine: ISearchEngine, protected val router: IRouter,
                                   private val deleteEntityService: DeleteEntityService) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastSeriesSearch: SeriesSearch? = null


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        CommonComponent.component.inject(this)

        eventBus.register(eventBusListener)
    }

    fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    fun searchSeries(searchTerm: String, searchCompleted: ((List<Series>) -> Unit)? = null) {
        lastSeriesSearch?.interrupt()

        lastSearchTermProperty = searchTerm

        lastSeriesSearch = SeriesSearch(searchTerm) { result ->
            seriesListView.showEntities(result)

            searchCompleted?.invoke(result)
        }

        searchEngine.searchSeries(lastSeriesSearch!!)
    }


    fun editSeries(series: Series) {
        router.showEditSeriesView(series)
    }

    fun deleteSeriesAsync(series: Series) {
        deleteEntityService.deleteSeriesAsync(series)
    }


    inner class EventBusListener {

        @Handler()
        fun seriesChanged(seriesChange: SeriesChanged) {
            searchSeries(lastSearchTermProperty)
        }

    }

}