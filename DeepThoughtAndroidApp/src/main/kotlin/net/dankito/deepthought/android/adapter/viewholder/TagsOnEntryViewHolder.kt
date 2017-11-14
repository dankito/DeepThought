package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.list_item_tag_on_entry.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import net.dankito.deepthought.android.views.SwipeButton
import java.util.*


class TagsOnEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsTagOnEntry: IsAddedToEntityView = itemView.vwIsTagOnEntry

    val btnEditTag: SwipeButton = itemView.btnEditTag

    val btnDeleteTag: SwipeButton = itemView.btnDeleteTag

    var lastItemSwipeTime: Date? = null

}