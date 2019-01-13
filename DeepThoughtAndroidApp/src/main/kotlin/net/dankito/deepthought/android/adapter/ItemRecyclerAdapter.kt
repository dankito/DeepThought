package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ItemViewHolder
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.getItemPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.sourcePreview
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.deepthought.ui.presenter.ItemsListPresenterBase


class ItemRecyclerAdapter(private val presenter: ItemsListPresenterBase): MultiSelectListRecyclerSwipeAdapter<Item, ItemViewHolder>() {

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()

    private val recycledTagViews = ArrayList<View>()


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.itemSwipeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_item, parent, false)

        val viewHolder = ItemViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindViewForNullValue(viewHolder: ItemViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.txtSourcePreview.visibility = View.GONE
        viewHolder.txtItemPreview.visibility = View.GONE
        viewHolder.lytItemTags.visibility = View.GONE
    }

    override fun bindItemToView(viewHolder: ItemViewHolder, item: Item) {
        var sourcePreview = item.sourcePreview
        if(sourcePreview.isNullOrBlank() && item.summary.isNullOrBlank() == false) {
            sourcePreview = item.summaryPlainText
        }

        viewHolder.txtSourcePreview.visibility = if (sourcePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtSourcePreview.text = sourcePreview

        viewHolder.txtItemPreview.visibility = View.VISIBLE
        viewHolder.txtItemPreview.text = item.getItemPreviewWithSeriesAndPublishingDate(item.source)
        setTxtItemPreviewMaxLines(viewHolder.txtItemPreview, viewHolder.txtSourcePreview, item)

        viewHolder.lytItemTags.visibility = if (item.hasTags()) View.VISIBLE else View.GONE
        tagsPreviewViewHelper.showTagsPreview(viewHolder.lytItemTags, item.tags, recycledTagViews)
    }

    override fun setupSwipeView(viewHolder: ItemViewHolder, item: Item) {
        viewHolder.btnShareItem.visibility = if (item.source?.url.isNullOrBlank() == false) View.VISIBLE else View.GONE
        viewHolder.btnShareItem.setOnClickListener {
            presenter.copySourceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteItem.setOnClickListener {
            presenter.deleteItemAsync(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun setTxtItemPreviewMaxLines(txtItemPreview: TextView, txtSourcePreview: TextView, item: Item) {
        val isShowingSourcePreview = txtSourcePreview.visibility == View.VISIBLE
        var countPreviewLines = if(isShowingSourcePreview) 5 else 6

        if(txtSourcePreview.lineCount == 2 || (txtSourcePreview.lineCount == 0 && txtSourcePreview.text.length >= 46)) { // txtSourcePreview.lineCount == 0 -> lineCount not calculated yet
            countPreviewLines--
        }

        if(item.hasTags()) {
            countPreviewLines--

            if(isShowingSourcePreview) { // if an item has a source and tag(s) as well, reduce by another line due to space between lines
                countPreviewLines--
            }
        }

        txtItemPreview.setLines(countPreviewLines)
    }

}