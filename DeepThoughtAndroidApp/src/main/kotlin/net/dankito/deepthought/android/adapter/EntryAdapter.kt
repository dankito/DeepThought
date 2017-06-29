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
import net.dankito.deepthought.ui.presenter.EntriesListPresenter


class EntryAdapter(private val presenter: EntriesListPresenter): ListSwipeAdapter<Entry>() {


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun generateView(position: Int, parent: ViewGroup?): View {
        return LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)
    }

    override fun fillValues(position: Int, convertView: View) {
        val entry = getItem(position)

        convertView.txtReferencePreview.visibility = if(entry.hasReference()) View.VISIBLE else View.GONE
        convertView.txtReferencePreview.text = entry.referencePreview

        convertView.txtEntryPreview.text = entry.entryPreview
        setTxtEntryPreviewMaxLines(convertView.txtEntryPreview, entry)

        convertView.txtEntryTags.visibility = if(entry.hasTags()) View.VISIBLE else View.GONE
        convertView.txtEntryTags.text = entry.tagsPreview

        convertView.btnShareEntry.setOnClickListener { presenter.shareReferenceUrl(entry) }

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