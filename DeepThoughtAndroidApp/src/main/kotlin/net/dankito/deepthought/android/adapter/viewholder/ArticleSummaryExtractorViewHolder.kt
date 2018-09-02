package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_article_summary_extractor.view.*
import net.dankito.utils.android.ui.view.SwipeButton


class ArticleSummaryExtractorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val txtExtractorName: TextView = itemView.txtExtractorName

    val btnIsFavorite: ImageButton = itemView.btnIsFavorite

    val txtFavoriteIndex: TextView = itemView.txtFavoriteIndex

    val btnEditArticleSummaryExtractorConfig: SwipeButton = itemView.btnEditArticleSummaryExtractorConfig

    val btnDeleteArticleSummaryExtractorConfig: SwipeButton = itemView.btnDeleteArticleSummaryExtractorConfig

}