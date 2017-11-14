package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_reference.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import net.dankito.deepthought.android.views.SwipeButton


class ReferenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsReferenceSetOnEntity: IsAddedToEntityView = itemView.vwIsReferenceSetOnEntity

    val imgChevronRight: ImageView = itemView.imgChevronRight

    val btnEditReference: SwipeButton = itemView.btnEditReference

    val btnShareReference: SwipeButton = itemView.btnShareReference

    val btnDeleteReference: SwipeButton = itemView.btnDeleteReference

}