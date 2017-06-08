package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_article_summary_item.view.*
import net.dankito.deepthought.android.R
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem


class ArticleSummaryAdapter : ListAdapter<ArticleSummaryItem>() {

    fun setArticleSummary(summary: ArticleSummary) {
        setItems(ArrayList(summary.articles))
    }

    fun moreItemsHaveBeenLoaded(summary: ArticleSummary) {
        addItems(summary.articles)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val articleSummaryItem = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_item, parent, false)

        view.txtTitle.visibility = if(articleSummaryItem.title.isBlank()) { GONE } else { VISIBLE }
        view.txtTitle.text = articleSummaryItem.title
        view.txtSummary.text = articleSummaryItem.summary

        Picasso.with(view?.context)
                .load(articleSummaryItem.previewImageUrl)
                .into(view.imgPreviewImage)

        return view
    }

}