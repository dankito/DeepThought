package net.dankito.deepthought.android.adapter

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ArticleSummaryItemViewHolder
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummaryItem


class ArticleSummaryItemRecyclerAdapter(activity: AppCompatActivity, private val presenter: ArticleSummaryPresenter):
        MultiSelectListRecyclerSwipeAdapter<ArticleSummaryItem, ArticleSummaryItemViewHolder>(activity) {

    override fun getSwipeLayoutResourceId(position: Int) = 0


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArticleSummaryItemViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_item, parent, false)

        return ArticleSummaryItemViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ArticleSummaryItemViewHolder, position: Int) {
        val item = getItem(position)

        if(item == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindTagToView(viewHolder, item)
            itemBound(viewHolder, item, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: ArticleSummaryItemViewHolder) {
        viewHolder.txtTitle.visibility = View.INVISIBLE

        viewHolder.txtSummary.visibility = View.INVISIBLE

        viewHolder.imgPreviewImage.visibility = View.INVISIBLE
    }

    private fun bindTagToView(viewHolder: ArticleSummaryItemViewHolder, item: ArticleSummaryItem) {
        viewHolder.txtTitle.visibility = if(item.title.isBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = item.title

        viewHolder.txtSummary.visibility = View.VISIBLE
        viewHolder.txtSummary.text = item.summary

        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        Picasso.with(viewHolder.itemView.context)
                .load(item.previewImageUrl)
                .into(viewHolder.imgPreviewImage)

    }

}