package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_file.view.*
import net.dankito.utils.android.ui.view.SwipeButton


class FileLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val txtFileName: TextView = itemView.txtFileName

    val txtFileSize: TextView = itemView.txtFileSize

    val txtFilePath: TextView = itemView.txtFilePath

    val btnOpenInDeepThought: SwipeButton = itemView.btnOpenInDeepThought

    val btnOpenContainingFolder: SwipeButton = itemView.btnOpenContainingFolder

    val btnRemoveFile: SwipeButton = itemView.btnRemoveFile

}