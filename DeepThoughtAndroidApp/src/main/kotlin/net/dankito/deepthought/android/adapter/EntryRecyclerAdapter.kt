package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.EntryViewHolder
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.ui.presenter.EntriesListPresenterBase


class EntryRecyclerAdapter(private val presenter: EntriesListPresenterBase): MultiSelectListRecyclerSwipeAdapter<Item, EntryViewHolder>() {

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()


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
        val referencePreview = item.source.preview
        viewHolder.txtReferencePreview.visibility = if (referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtReferencePreview.text = referencePreview

        viewHolder.txtEntryPreview.text = item.getEntryPreviewWithSeriesAndPublishingDate(item.source)
        setTxtEntryPreviewMaxLines(viewHolder.txtEntryPreview, viewHolder.txtReferencePreview, item)

        viewHolder.lytEntryTags.visibility = if (item.hasTags()) View.VISIBLE else View.GONE
        tagsPreviewViewHelper.showTagsPreview(viewHolder.lytEntryTags, item.tags)
    }

    override fun setupSwipeView(viewHolder: EntryViewHolder, item: Item) {
        viewHolder.btnShareEntry.visibility = if (item.source != null) View.VISIBLE else View.GONE
        viewHolder.btnShareEntry.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteEntry.setOnClickListener {
            presenter.deleteEntry(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun setTxtEntryPreviewMaxLines(txtEntryPreview: TextView, txtReferencePreview: TextView, item: Item) {
        var countPreviewLines = if(item.hasSource()) 4 else 5

        if(txtReferencePreview.lineCount == 2 || txtReferencePreview.text.length >= 50) {
            countPreviewLines--
        }

        if(item.hasTags()) {
            countPreviewLines--
        }

        txtEntryPreview.setLines(countPreviewLines)
    }

}