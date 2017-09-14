package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReferenceViewHolder
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.ui.presenter.ReferencesPresenterBase


abstract class ReferenceRecyclerAdapterBase(private val presenter: ReferencesPresenterBase): MultiSelectListRecyclerSwipeAdapter<Reference, ReferenceViewHolder>() {

    abstract val shouldShowImageIsReferenceAddedToEntry: Boolean

    abstract val shouldShowChevronRight: Boolean

    abstract val shouldShowButtonEditReference: Boolean


    override fun getSwipeLayoutResourceId(position: Int) = R.id.referenceSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        val viewHolder = ReferenceViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindItemToView(viewHolder: ReferenceViewHolder, item: Reference) {
        viewHolder.txtReferenceTitle.text = item.preview

        val seriesPreview = item.seriesAndPublishingDatePreview
        viewHolder.txtReferenceSeriesAndPublishingDate.text = seriesPreview
        viewHolder.txtReferenceSeriesAndPublishingDate.visibility = if(seriesPreview.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.imgIsReferenceAddedToEntry.visibility = if(shouldShowImageIsReferenceAddedToEntry) View.VISIBLE else View.GONE
        viewHolder.txtChevronRight.visibility = if(shouldShowChevronRight) View.VISIBLE else View.GONE
    }

    override fun setupSwipeView(viewHolder: ReferenceViewHolder, item: Reference) {
        viewHolder.btnEditReference.visibility = if(shouldShowButtonEditReference) View.VISIBLE else View.GONE
        viewHolder.btnShareReference.visibility = if(item.url.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.btnEditReference.setOnClickListener {
            presenter.editReference(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnShareReference.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteReference.setOnClickListener {
            presenter.deleteReference(item)
            closeSwipeView(viewHolder)
        }
    }

}