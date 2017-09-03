package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.daimajia.swipe.SwipeLayout
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.EntryViewHolder
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.extensions.referencePreview
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
            itemBound(viewHolder, entry)
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
        viewHolder.txtReferencePreview.visibility = if (entry.hasReference()) View.VISIBLE else View.GONE
        viewHolder.txtReferencePreview.text = entry.referencePreview

        viewHolder.txtEntryPreview.text = entry.preview
        setTxtEntryPreviewMaxLines(viewHolder.txtEntryPreview, entry)

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


    private fun setTxtEntryPreviewMaxLines(txtEntryPreview: TextView, entry: Entry) {
        var countPreviewLines = if(entry.hasReference()) 3 else 4

        if(entry.hasTags() == false) {
            countPreviewLines++
        }

        txtEntryPreview.setLines(countPreviewLines)
    }

}