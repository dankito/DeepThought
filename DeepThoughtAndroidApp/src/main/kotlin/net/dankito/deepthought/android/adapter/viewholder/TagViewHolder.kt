package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_tag.view.*


class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTagDisplayText: TextView = itemView.txtTagDisplayText

    val lytFilterIconClickArea: View = itemView.lytFilterIconClickArea

    val imgFilter: ImageView = itemView.imgFilter

    val btnEditTag: ImageButton = itemView.btnEditTag

    val btnDeleteTag: ImageButton = itemView.btnDeleteTag

}