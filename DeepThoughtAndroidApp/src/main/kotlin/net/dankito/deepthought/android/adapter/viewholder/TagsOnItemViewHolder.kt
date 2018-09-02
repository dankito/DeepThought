package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.list_item_tag_on_item.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import net.dankito.utils.android.ui.view.SwipeButton
import java.util.*


class TagsOnItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsTagOnItem: IsAddedToEntityView = itemView.vwIsTagOnItem

    val btnEditTag: SwipeButton = itemView.btnEditTag

    val btnDeleteTag: SwipeButton = itemView.btnDeleteTag

    var lastItemSwipeTime: Date? = null

}