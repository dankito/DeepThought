package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_entry.view.*
import net.dankito.deepthought.android.views.SwipeButton


class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtReferencePreview: TextView = itemView.txtReferencePreview

    val txtEntryPreview: TextView = itemView.txtEntryPreview

    val lytEntryTags: LinearLayout = itemView.lytEntryTags

    val btnShareEntry: SwipeButton = itemView.btnShareEntry

    val btnDeleteEntry: SwipeButton = itemView.btnDeleteEntry

}