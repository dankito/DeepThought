package net.dankito.deepthought.android.adapter

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ArticleSummaryItemViewHolder
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummaryItem


class ArticleSummaryItemRecyclerAdapter(activity: AppCompatActivity, private val presenter: ArticleSummaryPresenter):
        MultiSelectListRecyclerSwipeAdapter<ArticleSummaryItem, ArticleSummaryItemViewHolder>(activity) {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.readLaterArticleSwipeLayout


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
            bindItemToView(viewHolder, item)
            itemBound(viewHolder, item, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: ArticleSummaryItemViewHolder) {
        viewHolder.txtTitle.visibility = View.INVISIBLE

        viewHolder.txtSummary.visibility = View.INVISIBLE

        viewHolder.imgPreviewImage.visibility = View.INVISIBLE

        (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = false
    }

    private fun bindItemToView(viewHolder: ArticleSummaryItemViewHolder, item: ArticleSummaryItem) {
        viewHolder.txtTitle.visibility = if(item.title.isBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = item.title

        viewHolder.txtSummary.visibility = View.VISIBLE
        viewHolder.txtSummary.text = item.summary

        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        Picasso.with(viewHolder.itemView.context)
                .load(item.previewImageUrl)
                .into(viewHolder.imgPreviewImage)

        (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = true

        viewHolder.btnSaveArticleSummaryItemForLaterReading.visibility = View.VISIBLE
        viewHolder.btnSaveArticleSummaryItem.visibility = View.VISIBLE
//        viewHolder.btnShareArticleSummaryItem.visibility = if(item.url.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.btnSaveArticleSummaryItemForLaterReading.setOnClickListener { presenter.getAndSaveArticleForLaterReading(item) }
        viewHolder.btnSaveArticleSummaryItem.setOnClickListener { presenter.getAndSaveArticle(item) }
//        viewHolder.btnShareArticleSummaryItem.setOnClickListener { presenter.copyReferenceUrlToClipboard(item) } // TODO: actually there should also be the option to share article's text

    }

}