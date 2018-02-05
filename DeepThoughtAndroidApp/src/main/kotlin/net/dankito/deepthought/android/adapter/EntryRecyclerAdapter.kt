package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.EntryViewHolder
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.ui.presenter.ItemsListPresenterBase


class EntryRecyclerAdapter(private val presenter: ItemsListPresenterBase): MultiSelectListRecyclerSwipeAdapter<Item, EntryViewHolder>() {

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()

    private val recycledTagViews = ArrayList<View>()


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.entrySwipeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EntryViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        val viewHolder = EntryViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }

    override fun bindViewForNullValue(viewHolder: EntryViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.txtReferencePreview.visibility = View.GONE
        viewHolder.txtEntryPreview.visibility = View.GONE
        viewHolder.lytEntryTags.visibility = View.GONE
    }

    override fun bindItemToView(viewHolder: EntryViewHolder, item: Item) {
        var referencePreview = item.source.preview
        if(referencePreview.isNullOrBlank() && item.summary.isNullOrBlank() == false) {
            referencePreview = item.abstractPlainText
        }

        viewHolder.txtReferencePreview.visibility = if (referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtReferencePreview.text = referencePreview

        viewHolder.txtEntryPreview.visibility = View.VISIBLE
        viewHolder.txtEntryPreview.text = item.getEntryPreviewWithSeriesAndPublishingDate(item.source)
        setTxtEntryPreviewMaxLines(viewHolder.txtEntryPreview, viewHolder.txtReferencePreview, item)

        viewHolder.lytEntryTags.visibility = if (item.hasTags()) View.VISIBLE else View.GONE
        tagsPreviewViewHelper.showTagsPreview(viewHolder.lytEntryTags, item.tags, recycledTagViews)
    }

    override fun setupSwipeView(viewHolder: EntryViewHolder, item: Item) {
        viewHolder.btnShareEntry.visibility = if (item.source?.url.isNullOrBlank() == false) View.VISIBLE else View.GONE
        viewHolder.btnShareEntry.setOnClickListener {
            presenter.copySourceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteEntry.setOnClickListener {
            presenter.deleteItem(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun setTxtEntryPreviewMaxLines(txtEntryPreview: TextView, txtReferencePreview: TextView, item: Item) {
        val isShowingReferencePreview = txtReferencePreview.visibility == View.VISIBLE
        var countPreviewLines = if(isShowingReferencePreview) 5 else 6

        if(txtReferencePreview.lineCount == 2 || (txtReferencePreview.lineCount == 0 && txtReferencePreview.text.length >= 46)) { // txtReferencePreview.lineCount == 0 -> lineCount not calculated yet
            countPreviewLines--
        }

        if(item.hasTags()) {
            countPreviewLines--

            if(isShowingReferencePreview) { // if an item has a reference and tag(s) as well, reduce by another line due to space between lines
                countPreviewLines--
            }
        }

        txtEntryPreview.setLines(countPreviewLines)
    }

}