package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_tag_on_entry.view.*


class TagsOnEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imgIsTagAddedToEntry: ImageView = itemView.imgIsTagAddedToEntry

    val txtvwTagName: TextView = itemView.txtvwTagName

    val btnEditTag: ImageButton = itemView.btnEditTag

    val btnDeleteTag: ImageButton = itemView.btnDeleteTag

}