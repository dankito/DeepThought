package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_source.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.SourceViewHolder
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.ui.presenter.SourcePresenterBase


abstract class SourceRecyclerAdapterBase(private val presenter: SourcePresenterBase): MultiSelectListRecyclerSwipeAdapter<Source, SourceViewHolder>() {

    abstract val shouldShowImageIsSourceAddedToItem: Boolean

    abstract val shouldShowChevronRight: Boolean

    abstract val shouldShowButtonEditSource: Boolean

    protected open fun isAddedToEntity(source: Source): Boolean {
        return false
    }


    override fun getSwipeLayoutResourceId(position: Int) = R.id.sourceSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_source, parent, false)

        itemView.vwIsSourceSetOnEntity.isShowAddedViewEnabled = shouldShowImageIsSourceAddedToItem
        itemView.vwIsSourceSetOnEntity.setEntityNameTextSizeToHeader1TextSize()

        val viewHolder = SourceViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindViewForNullValue(viewHolder: SourceViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.vwIsSourceSetOnEntity.showState("", false)

        viewHolder.imgChevronRight.visibility = View.GONE
    }

    override fun bindItemToView(viewHolder: SourceViewHolder, item: Source) {
        var seriesPreview: String? = item.seriesAndPublishingDatePreview
        if(seriesPreview.isNullOrBlank()) seriesPreview = null

        val isAddedToEntity = shouldShowImageIsSourceAddedToItem && isAddedToEntity(item)

        viewHolder.vwIsSourceSetOnEntity.showState(item.preview, isAddedToEntity, seriesPreview)

        viewHolder.imgChevronRight.visibility = if(shouldShowChevronRight) View.VISIBLE else View.GONE
    }

    override fun setupSwipeView(viewHolder: SourceViewHolder, item: Source) {
        viewHolder.btnEditSource.visibility = if(shouldShowButtonEditSource) View.VISIBLE else View.GONE
        viewHolder.btnShareSource.visibility = if(item.url.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.btnEditSource.setOnClickListener {
            presenter.editSource(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnShareSource.setOnClickListener {
            presenter.copySourceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteSource.setOnClickListener {
            presenter.deleteSource(item)
            closeSwipeView(viewHolder)
        }
    }

}