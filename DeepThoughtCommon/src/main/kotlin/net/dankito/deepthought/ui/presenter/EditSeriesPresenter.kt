package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool


class EditSeriesPresenter(searchEngine: ISearchEngine, router: IRouter, deleteEntityService: DeleteEntityService, private val seriesService: SeriesService,
                          private val threadPool: IThreadPool) : SeriesPresenterBase(searchEngine, router, deleteEntityService) {


    fun saveSeriesAsync(series: Series, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            saveSeries(series)
            callback(true)
        }
    }

    fun saveSeries(series: Series) {
        // may extract SeriesPersister
        if(series.isPersisted()) {
            seriesService.update(series)
        }
        else {
            seriesService.persist(series)
        }
    }


    fun editSeries(reference: Reference, series: Series?) {
        router.showEditReferenceSeriesView(reference, series)
    }

}