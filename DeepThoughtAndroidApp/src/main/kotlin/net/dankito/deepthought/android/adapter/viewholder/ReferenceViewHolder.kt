package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_reference.view.*


class ReferenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtReferenceTitle: TextView = itemView.txtReferenceTitle

    val txtReferenceSeriesAndPublishingDate: TextView = itemView.txtReferenceSeriesAndPublishingDate

}