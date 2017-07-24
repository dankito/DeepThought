package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.ReferenceService
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class ReferencePersister(private val referenceService: ReferenceService) {

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

    private fun saveReference(reference: Reference): Boolean {
        if(reference.isPersisted() == false) {
            referenceService.persist(reference)
        }
        else {
            referenceService.update(reference)
        }

        return true
    }

}