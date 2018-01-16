package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_file.view.*
import net.dankito.deepthought.android.views.SwipeButton


class FileLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtFileName: TextView = itemView.txtFileName

    val txtFilePath: TextView = itemView.txtFilePath

    val btnRemoveFile: SwipeButton = itemView.btnRemoveFile

}