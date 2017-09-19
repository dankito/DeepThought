package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_series.view.*


class SeriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtSeriesTitle: TextView = itemView.txtSeriesTitle

    val imgIsSeriesSetOnReference: ImageView = itemView.imgIsSeriesSetOnReference

    val txtChevronRight: TextView = itemView.txtChevronRight

    val btnEditSeries: ImageButton = itemView.btnEditSeries

    val btnDeleteSeries: ImageButton = itemView.btnDeleteSeries

}