package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_reference.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView


class ReferenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsReferenceSetOnEntity: IsAddedToEntityView = itemView.vwIsReferenceSetOnEntity

    val txtChevronRight: TextView = itemView.txtChevronRight

    val btnEditReference: ImageButton = itemView.btnEditReference

    val btnShareReference: ImageButton = itemView.btnShareReference

    val btnDeleteReference: ImageButton = itemView.btnDeleteReference

}