package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_tag_on_entry.view.*
import kotlinx.android.synthetic.main.view_is_added_to_entity.view.*
import java.util.*


class TagsOnEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val vwIsAddedToEntityBorder: View = itemView.vwIsAddedToEntityBorder

    val imgIsAddedToEntity: ImageView = itemView.imgIsAddedToEntity

    val txtvwEntityName: TextView = itemView.txtvwEntityName

    val btnEditTag: ImageButton = itemView.btnEditTag

    val btnDeleteTag: ImageButton = itemView.btnDeleteTag

    var lastItemSwipeTime: Date? = null

}