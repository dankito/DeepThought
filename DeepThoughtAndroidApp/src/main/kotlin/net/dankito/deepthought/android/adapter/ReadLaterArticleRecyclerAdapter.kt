package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReadLaterArticleViewHolder
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter


class ReadLaterArticleRecyclerAdapter(private val presenter: ReadLaterArticleListPresenter): MultiSelectListRecyclerSwipeAdapter<ReadLaterArticle, ReadLaterArticleViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.readLaterArticleSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReadLaterArticleViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_read_later_article, parent, false)

        return ReadLaterArticleViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: ReadLaterArticleViewHolder, item: ReadLaterArticle) {
        viewHolder.txtTitle.visibility = if(item.referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = item.referencePreview

        viewHolder.txtSummary.text = item.entryPreview
        viewHolder.txtSummary.maxLines = 5

        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        Picasso.with(viewHolder.itemView.context)
                .load(item.previewImageUrl)
                .into(viewHolder.imgPreviewImage)
    }

    override fun setupSwipeView(viewHolder: ReadLaterArticleViewHolder, item: ReadLaterArticle) {
        viewHolder.btnSaveReadLaterArticle.setOnClickListener {
            presenter.saveAndDeleteReadLaterArticle(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnShareReadLaterArticle.visibility = if(item.entryExtractionResult?.reference?.url?.isNullOrBlank() ?: false) View.GONE else View.VISIBLE
        viewHolder.btnShareReadLaterArticle.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(item) // TODO: actually there should also be the option to share article's text
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteReadLaterArticle.visibility = View.VISIBLE
        viewHolder.btnDeleteReadLaterArticle.setOnClickListener {
            presenter.deleteReadLaterArticle(item)
            closeSwipeView(viewHolder)
        }
    }

}