package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.SeriesService
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class ReferencePersister(private val referenceService: ReferenceService, private val seriesService: SeriesService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun saveReferenceAsync(reference: Reference, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveReference(reference))
        }
    }

    fun saveReference(reference: Reference): Boolean {
        if(reference.series != null && reference.series?.isPersisted() == false) { // series has been deleted in the meantime
            reference.series?.let { series ->
                reference.series = null

                seriesService.persist(series)

                reference.series = series
            }

        }

        if(reference.isPersisted() == false) {
            referenceService.persist(reference)
        }
        else {
            referenceService.update(reference)
        }

        reference.series?.let { series -> // TODO: check if series changed
            seriesService.update(series) // reference is now persisted so series needs an update to store reference's id
        }

        return true
    }

}