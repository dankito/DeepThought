package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Series
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.SeriesService
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class SeriesPersister(private val seriesService: SeriesService, private val referenceService: ReferenceService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun saveSeriesAsync(series: Series, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveSeries(series))
        }
    }

    fun saveSeries(series: Series): Boolean {
        // may extract SeriesPersister
        if(series.isPersisted()) {
            seriesService.update(series)
        }
        else {
            seriesService.persist(series)
        }

        return true
    }

}