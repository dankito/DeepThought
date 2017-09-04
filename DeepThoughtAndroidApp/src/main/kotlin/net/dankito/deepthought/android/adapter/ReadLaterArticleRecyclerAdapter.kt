package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReadLaterArticleViewHolder
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter


class ReadLaterArticleRecyclerAdapter(private val presenter: ReadLaterArticleListPresenter): ListRecyclerSwipeAdapter<ReadLaterArticle, ReadLaterArticleViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = 0


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReadLaterArticleViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_item, parent, false)

        return ReadLaterArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ReadLaterArticleViewHolder, position: Int) {
        val readLaterArticle = getItem(position)
        val extractionResult = readLaterArticle?.entryExtractionResult

        if(extractionResult == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindTagToView(viewHolder, extractionResult)
            itemBound(viewHolder, readLaterArticle, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: ReadLaterArticleViewHolder) {
        viewHolder.txtTitle.visibility = View.INVISIBLE
        viewHolder.txtSummary.visibility = View.INVISIBLE
        viewHolder.imgPreviewImage.visibility = View.INVISIBLE
    }

    private fun bindTagToView(viewHolder: ReadLaterArticleViewHolder, extractionResult: EntryExtractionResult) {
        val referencePreview = extractionResult.reference?.preview

        viewHolder.txtTitle.visibility = if(referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = referencePreview

        viewHolder.txtSummary.visibility = View.VISIBLE
        viewHolder.txtSummary.text = extractionResult.entry.preview

        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        Picasso.with(viewHolder.itemView.context)
                .load(extractionResult.reference?.previewImageUrl)
                .into(viewHolder.imgPreviewImage)

    }

}