package net.dankito.deepthought.android.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.dankito.deepthought.android.adapter.viewholder.SeriesViewHolder
import net.dankito.deepthought.android.extensions.setTintListToEntityIsSelectedColor
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.presenter.SeriesPresenterBase


class SeriesOnReferenceRecyclerAdapter(presenter: SeriesPresenterBase): SeriesRecyclerAdapterBase(presenter) {

    var selectedSeries: Series? = null


    override val shouldShowImageIsSeriesSetOnReference: Boolean
        get() = true

    override val shouldShowChevronRight: Boolean
        get() = false

    override val shouldShowButtonEditSeries: Boolean
        get() = false


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SeriesViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        viewHolder.imgIsSeriesSetOnReference.setTintListToEntityIsSelectedColor()

        return viewHolder
    }


    override fun itemBound(viewHolder: RecyclerView.ViewHolder, item: Series, position: Int) {
        super.itemBound(viewHolder, item, position)

        viewHolder.itemView.isActivated = selectedSeries == item
    }

}