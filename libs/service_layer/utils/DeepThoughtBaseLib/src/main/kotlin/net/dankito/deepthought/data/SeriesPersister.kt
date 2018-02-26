package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.Series
import net.dankito.service.data.SeriesService
import net.dankito.util.IThreadPool
import javax.inject.Inject


class SeriesPersister(private val seriesService: SeriesService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        BaseComponent.component.inject(this)
    }


    fun saveSeriesAsync(series: Series, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveSeries(series))
        }
    }

    fun saveSeries(series: Series): Boolean {
        // may extract SeriesPersister
        if(series.isPersisted()) {
            seriesService.update(series, true)
        }
        else {
            seriesService.persist(series)
        }

        return true
    }

}