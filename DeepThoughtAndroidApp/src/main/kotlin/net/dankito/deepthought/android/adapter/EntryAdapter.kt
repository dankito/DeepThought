package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.model.Entry


class EntryAdapter: ListAdapter<Entry>() {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        val entry = getItem(position)


        view.txtReferencePreview.visibility = if(entry.hasReference()) View.VISIBLE else View.GONE
        view.txtReferencePreview.text = entry.referencePreview

        view.txtEntryPreview.text = entry.entryPreview

        view.txtEntryTags.visibility = if(entry.hasTags()) View.VISIBLE else View.GONE

        return view
    }

}