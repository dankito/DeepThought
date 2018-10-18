package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_source.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import net.dankito.utils.android.ui.view.SwipeButton


class SourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsSourceSetOnEntity: IsAddedToEntityView = itemView.vwIsSourceSetOnEntity

    val imgChevronRight: ImageView = itemView.imgChevronRight

    val btnEditSource: SwipeButton = itemView.btnEditSource

    val btnShareSource: SwipeButton = itemView.btnShareSource

    val btnDeleteSource: SwipeButton = itemView.btnDeleteSource

}