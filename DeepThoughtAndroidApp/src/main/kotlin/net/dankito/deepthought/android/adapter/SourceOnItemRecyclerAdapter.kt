package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.presenter.SourcePresenterBase


class SourceOnItemRecyclerAdapter(presenter: SourcePresenterBase): SourceRecyclerAdapterBase(presenter) {

    var selectedSource: Source? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override val shouldShowImageIsSourceAddedToItem: Boolean
        get() = true

    override val shouldShowChevronRight: Boolean
        get() = false

    override val shouldShowButtonEditSource: Boolean
        get() = false


    override fun isAddedToEntity(source: Source): Boolean {
        return selectedSource?.id == source.id
    }

}