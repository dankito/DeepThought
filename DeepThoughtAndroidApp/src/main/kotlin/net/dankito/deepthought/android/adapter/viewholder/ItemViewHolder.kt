package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_item.view.*
import net.dankito.utils.android.ui.view.SwipeButton


class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtSourcePreview: TextView = itemView.txtSourcePreview

    val txtItemPreview: TextView = itemView.txtItemPreview

    val lytItemTags: LinearLayout = itemView.lytItemTags

    val btnShareItem: SwipeButton = itemView.btnShareItem

    val btnDeleteItem: SwipeButton = itemView.btnDeleteItem

}