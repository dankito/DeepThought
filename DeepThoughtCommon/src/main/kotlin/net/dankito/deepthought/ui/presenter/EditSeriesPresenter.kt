package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool


class EditSeriesPresenter(seriesListView: ISeriesListView, searchEngine: ISearchEngine, router: IRouter, deleteEntityService: DeleteEntityService, private val seriesPersister: SeriesPersister,
                          private val threadPool: IThreadPool) : SeriesPresenterBase(seriesListView, searchEngine, router, deleteEntityService) {


    fun saveSeriesAsync(series: Series, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            saveSeries(series)
            callback(true)
        }
    }

    fun saveSeries(series: Series) {
        seriesPersister.saveSeries(series)
    }


    fun editSeries(source: Source, series: Series?) {
        router.showEditReferenceSeriesView(source, series)
    }

}