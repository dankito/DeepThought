package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.presenter.util.ReferencePersister


class EditReferencePresenter(private val referencePersister: ReferencePersister) {


    fun saveReferenceAsync(reference: Reference, callback: (Boolean) -> Unit) {
        referencePersister.saveReferenceAsync(reference, callback)
    }

}