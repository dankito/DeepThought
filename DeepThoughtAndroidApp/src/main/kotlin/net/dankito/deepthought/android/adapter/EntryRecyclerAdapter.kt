package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.EntryViewHolder
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.ui.presenter.EntriesListPresenterBase


class EntryRecyclerAdapter(private val presenter: EntriesListPresenterBase): MultiSelectListRecyclerSwipeAdapter<Entry, EntryViewHolder>() {

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.entrySwipeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EntryViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        return EntryViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: EntryViewHolder, item: Entry) {
        val referencePreview = item.reference.preview
        viewHolder.txtReferencePreview.visibility = if (referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtReferencePreview.text = referencePreview

        var preview = item.preview
        val seriesAndPublishingDate = item.reference.seriesAndPublishingDatePreview
        if(seriesAndPublishingDate.isNullOrBlank() == false) {
            preview = seriesAndPublishingDate + " | " + preview
        }
        viewHolder.txtEntryPreview.text = preview
        setTxtEntryPreviewMaxLines(viewHolder.txtEntryPreview, viewHolder.txtReferencePreview, item)

        viewHolder.lytEntryTags.visibility = if (item.hasTags()) View.VISIBLE else View.GONE
        tagsPreviewViewHelper.showTagsPreview(viewHolder.lytEntryTags, item.tags)
    }

    override fun setupSwipeView(viewHolder: EntryViewHolder, item: Entry) {
        viewHolder.btnShareEntry.visibility = if (item.reference != null) View.VISIBLE else View.GONE
        viewHolder.btnShareEntry.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteEntry.setOnClickListener {
            presenter.deleteEntry(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun setTxtEntryPreviewMaxLines(txtEntryPreview: TextView, txtReferencePreview: TextView, entry: Entry) {
        var countPreviewLines = if(entry.hasReference()) 4 else 5

        if(txtReferencePreview.lineCount == 2 || txtReferencePreview.text.length >= 50) {
            countPreviewLines--
        }

        if(entry.hasTags()) {
            countPreviewLines--
        }

        txtEntryPreview.setLines(countPreviewLines)
    }

}