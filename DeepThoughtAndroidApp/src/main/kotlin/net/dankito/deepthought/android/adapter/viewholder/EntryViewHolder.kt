package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_entry.view.*


class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtReferencePreview: TextView = itemView.txtReferencePreview

    val txtEntryPreview: TextView = itemView.txtEntryPreview

    val txtEntryTags: TextView = itemView.txtEntryTags

    val btnShareEntry: ImageButton = itemView.btnShareEntry

    val btnDeleteEntry: ImageButton = itemView.btnDeleteEntry

}