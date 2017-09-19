package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.SeriesSearch


abstract class SeriesPresenterBase(private var searchEngine: ISearchEngine, protected var router: IRouter,
                                       private val deleteEntityService: DeleteEntityService) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm


    fun searchSeries(searchTerm: String, searchCompleted: ((List<Series>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchSeries(SeriesSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        })
    }

    protected open fun retrievedSearchResults(result: List<Series>) {

    }


    fun editSeries(series: Series) {
        router.showEditSeriesView(series)
    }

    fun deleteSeries(series: Series) {
        deleteEntityService.deleteSeries(series)
    }

}