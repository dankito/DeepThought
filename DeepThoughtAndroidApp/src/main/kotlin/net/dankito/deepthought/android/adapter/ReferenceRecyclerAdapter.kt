package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReferenceViewHolder
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview


class ReferenceRecyclerAdapter(/*private val presenter: ReferencesListPresenter*/): ListRecyclerSwipeAdapter<Reference, ReferenceViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = 0


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
    }

    private fun bindTagToView(viewHolder: ReferenceViewHolder, reference: Reference) {
        viewHolder.txtReferenceTitle.visibility = View.VISIBLE
        viewHolder.txtReferenceTitle.text = reference.preview

        val seriesPreview = reference.seriesAndPublishingDatePreview
        viewHolder.txtReferenceSeriesAndPublishingDate.text = seriesPreview
        viewHolder.txtReferenceSeriesAndPublishingDate.visibility = if(seriesPreview.isNullOrBlank()) View.GONE else View.VISIBLE
    }

}