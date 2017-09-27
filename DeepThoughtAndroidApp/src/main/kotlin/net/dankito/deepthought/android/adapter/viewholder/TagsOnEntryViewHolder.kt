package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.list_item_tag_on_entry.view.*
import net.dankito.deepthought.android.views.IsAddedToEntityView
import java.util.*


class TagsOnEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsTagOnEntry: IsAddedToEntityView = itemView.vwIsTagOnEntry

    val btnEditTag: ImageButton = itemView.btnEditTag

    val btnDeleteTag: ImageButton = itemView.btnDeleteTag

    var lastItemSwipeTime: Date? = null

}