package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReferenceViewHolder
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.ui.presenter.EditReferencePresenter
import net.dankito.deepthought.ui.presenter.ReferencesPresenterBase


class ReferenceRecyclerAdapter(private val presenter: ReferencesPresenterBase): MultiSelectListRecyclerSwipeAdapter<Reference, ReferenceViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.referenceSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        return ReferenceViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: ReferenceViewHolder, item: Reference) {
        viewHolder.txtReferenceTitle.text = item.preview

        val seriesPreview = item.seriesAndPublishingDatePreview
        viewHolder.txtReferenceSeriesAndPublishingDate.text = seriesPreview
        viewHolder.txtReferenceSeriesAndPublishingDate.visibility = if(seriesPreview.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    override fun setupSwipeView(viewHolder: ReferenceViewHolder, item: Reference) {
        viewHolder.btnEditReference.visibility = if(presenter is EditReferencePresenter) View.GONE else View.VISIBLE
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