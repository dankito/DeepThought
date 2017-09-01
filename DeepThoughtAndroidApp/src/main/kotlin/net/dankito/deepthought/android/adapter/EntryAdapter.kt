package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.daimajia.swipe.SwipeLayout
import kotlinx.android.synthetic.main.list_item_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.extensions.tagsPreview
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.presenter.EntriesListPresenterBase


class EntryAdapter(private val presenter: EntriesListPresenterBase): ListSwipeAdapter<Entry>() {


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.entrySwipeLayout
    }

    override fun generateView(position: Int, parent: ViewGroup?): View {
        return LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)
    }

    override fun fillValues(position: Int, convertView: View) {
        val entry = getItem(position)

        if(entry == null) { // when an entry has been deleted in db on a synchronized device but index hasn't been updated yet
            convertView.txtReferencePreview.visibility = View.GONE
            convertView.txtEntryPreview.text = ""
            convertView.txtEntryTags.visibility = View.GONE
            convertView.btnShareEntry.setOnClickListener(null)
            convertView.btnDeleteEntry.setOnClickListener(null)
            return
        }

        convertView.txtReferencePreview.visibility = if(entry.hasReference()) View.VISIBLE else View.GONE
        convertView.txtReferencePreview.text = entry.referencePreview

        convertView.txtEntryPreview.text = entry.entryPreview
        setTxtEntryPreviewMaxLines(convertView.txtEntryPreview, entry)

        convertView.txtEntryTags.visibility = if(entry.hasTags()) View.VISIBLE else View.GONE
        convertView.txtEntryTags.text = entry.tagsPreview

        convertView.btnShareEntry.visibility = if(entry.reference != null) View.VISIBLE else View.GONE
        convertView.btnShareEntry.setOnClickListener {
            presenter.copyReferenceUrlToClipboard(entry)
            (convertView as? SwipeLayout)?.close()
        }

        convertView.btnDeleteEntry.setOnClickListener {
            presenter.deleteEntry(entry)
            (convertView as? SwipeLayout)?.close()
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