package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.presenter.ReferencesPresenterBase


class ReferenceOnEntryRecyclerAdapter(presenter: ReferencesPresenterBase): ReferenceRecyclerAdapterBase(presenter) {

    var selectedSource: Source? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override val shouldShowImageIsReferenceAddedToEntry: Boolean
        get() = true

    override val shouldShowChevronRight: Boolean
        get() = false

    override val shouldShowButtonEditReference: Boolean
        get() = false


    override fun isAddedToEntity(source: Source): Boolean {
        return selectedSource?.id == source.id
    }

}