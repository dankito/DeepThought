package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.daimajia.swipe.SwipeLayout
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.EntryViewHolder
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import net.dankito.deepthought.model.extensions.tagsPreview
import net.dankito.deepthought.ui.presenter.EntriesListPresenterBase


class EntryRecyclerAdapter(private val presenter: EntriesListPresenterBase): ListRecyclerSwipeAdapter<Entry, EntryViewHolder>() {


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.entrySwipeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EntryViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        return EntryViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: EntryViewHolder, position: Int) {
        val entry = getItem(position)

        if(entry == null) { // when an entry has been deleted in db on a synchronized device but index hasn't been updated yet
            bindViewForNullValue(viewHolder)
        }
        else {
            bindEntryToView(viewHolder, entry)
            itemBound(viewHolder, entry, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: EntryViewHolder) {
        viewHolder.txtReferencePreview.visibility = View.GONE
        viewHolder.txtEntryPreview.text = ""
        viewHolder.txtEntryTags.visibility = View.GONE
        viewHolder.btnShareEntry.setOnClickListener(null)
        viewHolder.btnDeleteEntry.setOnClickListener(null)
    }

    private fun bindEntryToView(viewHolder: EntryViewHolder, entry: Entry) {
        val referencePreview = entry.reference.preview
        viewHolder.txtReferencePreview.visibility = if (referencePreview.isNullOrBlank()) View.GONE else View.VISIBLE
        viewHolder.txtReferencePreview.text = referencePreview

        var preview = entry.preview
        val seriesAndPublishingDate = entry.reference.seriesAndPublishingDatePreview
        if(seriesAndPublishingDate.isNullOrBlank() == false) {
            preview = seriesAndPublishingDate + " | " + preview
        }
        viewHolder.txtEntryPreview.text = preview
        setTxtEntryPreviewMaxLines(viewHolder.txtEntryPreview, viewHolder.txtReferencePreview, entry)

        viewHolder.txtEntryTags.visibility = if (entry.hasTags()) View.VISIBLE else View.GONE
        viewHolder.txtEntryTags.text = entry.tagsPreview

        viewHolder.btnShareEntry.visibility = if (entry.reference != null) View.VISIBLE else View.GONE
        viewHolder.btnShareEntry.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(entry)
            (viewHolder.itemView as? SwipeLayout)?.close()
        }

        viewHolder.btnDeleteEntry.setOnClickListener {
            presenter.deleteEntry(entry)
            (viewHolder.itemView as? SwipeLayout)?.close()
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