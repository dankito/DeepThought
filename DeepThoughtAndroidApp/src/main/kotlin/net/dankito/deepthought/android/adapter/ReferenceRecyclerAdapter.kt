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


class ReferenceRecyclerAdapter(private val presenter: ReferencesPresenterBase): ListRecyclerSwipeAdapter<Reference, ReferenceViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.referenceSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        return ReferenceViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ReferenceViewHolder, position: Int) {
        val reference = getItem(position)

        if(reference == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindTagToView(viewHolder, reference)
            itemBound(viewHolder, reference, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: ReferenceViewHolder) {
        viewHolder.txtReferenceTitle.visibility = View.INVISIBLE
        viewHolder.txtReferenceSeriesAndPublishingDate.visibility = View.INVISIBLE

        viewHolder.btnEditReference.setOnClickListener {  }
        viewHolder.btnShareReference.setOnClickListener {  }
        viewHolder.btnDeleteReference.setOnClickListener {  }
    }

    private fun bindTagToView(viewHolder: ReferenceViewHolder, reference: Reference) {
        viewHolder.txtReferenceTitle.visibility = View.VISIBLE
        viewHolder.txtReferenceTitle.text = reference.preview

        val seriesPreview = reference.seriesAndPublishingDatePreview
        viewHolder.txtReferenceSeriesAndPublishingDate.text = seriesPreview
        viewHolder.txtReferenceSeriesAndPublishingDate.visibility = if(seriesPreview.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.btnShareReference.visibility = if(reference.url.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.btnEditReference.setOnClickListener { presenter.editReference(reference) }
        viewHolder.btnShareReference.setOnClickListener { presenter.copyReferenceUrlToClipboard(reference) }
        viewHolder.btnDeleteReference.setOnClickListener { presenter.deleteReference(reference) }
    }

}