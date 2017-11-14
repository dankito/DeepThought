package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_tag.view.*
import net.dankito.deepthought.android.views.SwipeButton


class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTagDisplayText: TextView = itemView.txtTagDisplayText

    val lytFilterIconClickArea: View = itemView.lytFilterIconClickArea

    val imgFilter: ImageView = itemView.imgFilter

    val btnEditTag: SwipeButton = itemView.btnEditTag

    val btnDeleteTag: SwipeButton = itemView.btnDeleteTag

}