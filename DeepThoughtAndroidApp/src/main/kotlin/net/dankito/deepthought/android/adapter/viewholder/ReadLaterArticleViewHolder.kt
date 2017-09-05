package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_read_later_article.view.*


class ReadLaterArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTitle: TextView = itemView.txtTitle

    val txtSummary: TextView = itemView.txtSummary

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val btnSaveReadLaterArticle: ImageButton = itemView.btnSaveArticleSummaryItemOrReadLaterArticle

    val btnShareReadLaterArticle: ImageButton = itemView.btnShareArticleSummaryItemOrReadLaterArticle

    val btnDeleteReadLaterArticle: ImageButton = itemView.btnDeleteReadLaterArticle

}