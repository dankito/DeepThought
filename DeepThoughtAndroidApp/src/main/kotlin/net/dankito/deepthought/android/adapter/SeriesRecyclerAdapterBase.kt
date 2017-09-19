package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.SeriesViewHolder
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.presenter.SeriesPresenterBase


abstract class SeriesRecyclerAdapterBase(private val presenter: SeriesPresenterBase): MultiSelectListRecyclerSwipeAdapter<Series, SeriesViewHolder>() {

    abstract val shouldShowImageIsSeriesSetOnReference: Boolean

    abstract val shouldShowChevronRight: Boolean

    abstract val shouldShowButtonEditSeries: Boolean


    override fun getSwipeLayoutResourceId(position: Int) = R.id.seriesSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SeriesViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_series, parent, false)

        val viewHolder = SeriesViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindItemToView(viewHolder: SeriesViewHolder, item: Series) {
        viewHolder.txtSeriesTitle.text = item.title

        viewHolder.imgIsSeriesSetOnReference.visibility = if(shouldShowImageIsSeriesSetOnReference) View.VISIBLE else View.GONE
        viewHolder.txtChevronRight.visibility = if(shouldShowChevronRight) View.VISIBLE else View.GONE
    }

    override fun setupSwipeView(viewHolder: SeriesViewHolder, item: Series) {
        viewHolder.btnEditSeries.visibility = if(shouldShowButtonEditSeries) View.VISIBLE else View.GONE

        viewHolder.btnEditSeries.setOnClickListener {
            presenter.editSeries(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteSeries.setOnClickListener {
            presenter.deleteSeries(item)
            closeSwipeView(viewHolder)
        }
    }

}