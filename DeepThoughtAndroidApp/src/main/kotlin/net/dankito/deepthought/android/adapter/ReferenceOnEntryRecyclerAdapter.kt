package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.presenter.ReferencesPresenterBase


class ReferenceOnEntryRecyclerAdapter(presenter: ReferencesPresenterBase): ReferenceRecyclerAdapterBase(presenter) {

    var selectedReference: Reference? = null


    override val shouldShowImageIsReferenceAddedToEntry: Boolean
        get() = true

    override val shouldShowChevronRight: Boolean
        get() = false

    override val shouldShowButtonEditReference: Boolean
        get() = false


    override fun isAddedToEntity(reference: Reference): Boolean {
        return selectedReference == reference
    }

}