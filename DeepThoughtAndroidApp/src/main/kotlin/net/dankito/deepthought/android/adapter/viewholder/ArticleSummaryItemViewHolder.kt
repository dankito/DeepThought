package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_read_later_article.view.*


class ArticleSummaryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTitle: TextView = itemView.txtTitle

    val txtSummary: TextView = itemView.txtSummary

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val btnSaveArticleSummaryItemForLaterReading: ImageButton = itemView.btnSaveArticleSummaryItemForLaterReading

    val btnSaveArticleSummaryItem: ImageButton = itemView.btnSaveArticleSummaryItemOrReadLaterArticle

    val btnShareArticleSummaryItem: ImageButton = itemView.btnShareArticleSummaryItemOrReadLaterArticle

}