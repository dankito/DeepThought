package net.dankito.deepthought.android.adapter

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ArticleSummaryItemViewHolder
import net.dankito.deepthought.model.extensions.MaxPreviewLength
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummaryItem


class ArticleSummaryItemRecyclerAdapter(activity: AppCompatActivity, private val presenter: ArticleSummaryPresenter):
        MultiSelectListRecyclerSwipeAdapter<ArticleSummaryItem, ArticleSummaryItemViewHolder>() {

    init {
        enableMultiSelectionMode(activity, R.menu.activity_article_summary_contextual_action_menu)
    }


    override fun getSwipeLayoutResourceId(position: Int) = R.id.readLaterArticleSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArticleSummaryItemViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_read_later_article, parent, false)

        val viewHolder = ArticleSummaryItemViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindItemToView(viewHolder: ArticleSummaryItemViewHolder, item: ArticleSummaryItem) {
        viewHolder.txtTitle.visibility = if(item.title.isBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = item.title

        var summary = item.summary
        if(summary.length > MaxPreviewLength) {
            summary = summary.substring(0, MaxPreviewLength) + "..."
        }
        viewHolder.txtSummary.text = summary

        Picasso.with(viewHolder.itemView.context)
                .load(item.previewImageUrl)
                .into(viewHolder.imgPreviewImage)
    }

    override fun setupSwipeView(viewHolder: ArticleSummaryItemViewHolder, item: ArticleSummaryItem) {
        viewHolder.btnSaveArticleSummaryItemForLaterReading.visibility = View.VISIBLE
        viewHolder.btnSaveArticleSummaryItemForLaterReading.setOnClickListener {
            presenter.getAndSaveArticleForLaterReading(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnSaveArticleSummaryItem.setOnClickListener {
            presenter.getAndSaveArticle(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnShareArticleSummaryItem.visibility = if(item.url.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.btnShareArticleSummaryItem.setOnClickListener {
            presenter.copySourceUrlToClipboard(item)  // TODO: actually there should also be the option to share article's text
            closeSwipeView(viewHolder)
        }
    }

}