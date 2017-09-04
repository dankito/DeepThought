package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_article_summary_item.view.*


class ArticleSummaryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTitle: TextView = itemView.txtTitle

    val txtSummary: TextView = itemView.txtSummary

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

}