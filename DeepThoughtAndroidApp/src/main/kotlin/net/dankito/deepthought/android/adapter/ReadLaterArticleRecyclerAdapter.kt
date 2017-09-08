package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.squareup.picasso.Picasso
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReadLaterArticleViewHolder
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.presenter.ReadLaterArticleListPresenter


class ReadLaterArticleRecyclerAdapter(private val presenter: ReadLaterArticleListPresenter): MultiSelectListRecyclerSwipeAdapter<ReadLaterArticle, ReadLaterArticleViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.readLaterArticleSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReadLaterArticleViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_read_later_article, parent, false)

        return ReadLaterArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ReadLaterArticleViewHolder, position: Int) {
        val readLaterArticle = getItem(position)
        val extractionResult = readLaterArticle?.entryExtractionResult

        if(extractionResult == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindArticleToView(viewHolder, readLaterArticle, extractionResult)
            itemBound(viewHolder, readLaterArticle, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: ReadLaterArticleViewHolder) {
        viewHolder.txtTitle.visibility = View.INVISIBLE
        viewHolder.txtSummary.visibility = View.INVISIBLE
        viewHolder.imgPreviewImage.visibility = View.INVISIBLE

        (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = false
    }

    private fun bindArticleToView(viewHolder: ReadLaterArticleViewHolder, article: ReadLaterArticle, extractionResult: EntryExtractionResult) {
        val referencePreview = extractionResult.reference?.preview

        viewHolder.txtTitle.visibility = if(referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtTitle.text = referencePreview

        viewHolder.txtSummary.visibility = View.VISIBLE
        var preview = extractionResult.entry.preview
        val seriesAndPublishingDate = extractionResult.reference.seriesAndPublishingDatePreview
        if(seriesAndPublishingDate.isNullOrBlank() == false) {
            preview = seriesAndPublishingDate + " | " + preview
        }
        viewHolder.txtSummary.text = preview
        viewHolder.txtSummary.maxLines = 5

        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        Picasso.with(viewHolder.itemView.context)
                .load(extractionResult.reference?.previewImageUrl)
                .into(viewHolder.imgPreviewImage)

        (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = true

        viewHolder.btnSaveReadLaterArticle.visibility = View.VISIBLE
        viewHolder.btnShareReadLaterArticle.visibility = if(extractionResult.reference?.url?.isNullOrBlank() ?: false) View.GONE else View.VISIBLE
        viewHolder.btnDeleteReadLaterArticle.visibility = View.VISIBLE

        viewHolder.btnSaveReadLaterArticle.setOnClickListener { presenter.saveAndDeleteReadLaterArticle(article) }
        viewHolder.btnShareReadLaterArticle.setOnClickListener { presenter.copyReferenceUrlToClipboard(article) } // TODO: actually there should also be the option to share article's text
        viewHolder.btnDeleteReadLaterArticle.setOnClickListener { presenter.deleteReadLaterArticle(article) }

    }

}