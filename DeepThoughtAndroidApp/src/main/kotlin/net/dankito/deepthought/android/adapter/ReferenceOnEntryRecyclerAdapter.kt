package net.dankito.deepthought.android.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.dankito.deepthought.android.adapter.viewholder.ReferenceViewHolder
import net.dankito.deepthought.android.extensions.setTintListToEntityIsSelectedColor
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


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        viewHolder.imgIsReferenceSetOnEntry.setTintListToEntityIsSelectedColor()

        return viewHolder
    }


    override fun itemBound(viewHolder: RecyclerView.ViewHolder, item: Reference, position: Int) {
        super.itemBound(viewHolder, item, position)

        viewHolder.itemView.isActivated = selectedReference == item
    }

}