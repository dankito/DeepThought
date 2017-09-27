package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_reference.view.*
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

    protected open fun isAddedToEntity(reference: Reference): Boolean {
        return false
    }


    override fun getSwipeLayoutResourceId(position: Int) = R.id.referenceSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        itemView.vwIsReferenceSetOnEntity.isShowAddedViewEnabled = shouldShowImageIsReferenceAddedToEntry

        val viewHolder = ReferenceViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindItemToView(viewHolder: ReferenceViewHolder, item: Reference) {
        var seriesPreview: String? = item.seriesAndPublishingDatePreview
        if(seriesPreview.isNullOrBlank()) seriesPreview = null

        val isAddedToEntity = shouldShowImageIsReferenceAddedToEntry && isAddedToEntity(item)

        viewHolder.vwIsReferenceSetOnEntity.showState(item.preview, isAddedToEntity, seriesPreview)

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