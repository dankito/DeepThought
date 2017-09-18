package net.dankito.deepthought.android.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_article_summary_extractor.view.*


class ArticleSummaryExtractorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

    val imgPreviewImage: ImageView = itemView.imgPreviewImage

    val txtExtractorName: TextView = itemView.txtExtractorName

    val chkIsFavorite: CheckBox = itemView.chkIsFavorite

    val txtFavoriteIndex: TextView = itemView.txtFavoriteIndex

    val btnEditArticleSummaryExtractorConfig: ImageButton = itemView.btnEditArticleSummaryExtractorConfig

    val btnDeleteArticleSummaryExtractorConfig: ImageButton = itemView.btnDeleteArticleSummaryExtractorConfig

}