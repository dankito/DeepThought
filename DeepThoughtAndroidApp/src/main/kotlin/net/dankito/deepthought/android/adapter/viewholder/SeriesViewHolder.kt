package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_series.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import net.dankito.deepthought.android.views.SwipeButton


class SeriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsSeriesSetOnReference: IsAddedToEntityView = itemView.vwIsSeriesSetOnReference

    val imgChevronRight: ImageView = itemView.imgChevronRight

    val btnEditSeries: SwipeButton = itemView.btnEditSeries

    val btnDeleteSeries: SwipeButton = itemView.btnDeleteSeries

}