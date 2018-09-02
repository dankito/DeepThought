package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_read_later_article.view.*
import net.dankito.utils.android.ui.view.SwipeButton


class ReadLaterArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtTitle: TextView = itemView.txtTitle

    val txtSummary: TextView = itemView.txtSummary

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val btnSaveReadLaterArticle: SwipeButton = itemView.btnSaveArticleSummaryItemOrReadLaterArticle

    val btnShareReadLaterArticle: SwipeButton = itemView.btnShareArticleSummaryItemOrReadLaterArticle

    val btnDeleteReadLaterArticle: SwipeButton = itemView.btnDeleteReadLaterArticle

}