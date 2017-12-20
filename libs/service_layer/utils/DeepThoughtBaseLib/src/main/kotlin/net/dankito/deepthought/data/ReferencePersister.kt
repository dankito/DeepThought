package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.SeriesService
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class ReferencePersister(private val referenceService: ReferenceService, private val seriesService: SeriesService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        BaseComponent.component.inject(this)
    }


    fun saveReferenceAsync(source: Source, series: Series?, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveReference(source, series))
        }
    }

    fun saveReference(source: Source): Boolean {
        return saveReference(source, source.series)
    }

    fun saveReference(source: Source, series: Series?, doChangesAffectDependentEntities: Boolean = true): Boolean {
        if(series != null && series.isPersisted() == false) { // if series has been newly created but not persisted yet
            seriesService.persist(series)
        }

        if(source.series != null && source.series?.isPersisted() == false) { // series has been deleted in the meantime
            source.series?.let { series ->
                source.series = null

                seriesService.persist(series) // TODO: but why saving it then again?

                source.series = series
            }

        }

        val previousSeries = source.series
        if(previousSeries != null && previousSeries?.id != series?.id) { // remove previous series
            source.series = null
            seriesService.update(previousSeries)
        }

        if(previousSeries?.id != series?.id) {
            source.series = series
        }

        val wasSourcePersisted = source.isPersisted()
        if(wasSourcePersisted == false) {
            referenceService.persist(source)
        }
        else {
            referenceService.update(source, doChangesAffectDependentEntities)
        }

        if(series?.id != previousSeries?.id || wasSourcePersisted == false) {
            series?.let { seriesService.update(series) } // source is now persisted so series needs an update to store source's id
        }

        return true
    }

}