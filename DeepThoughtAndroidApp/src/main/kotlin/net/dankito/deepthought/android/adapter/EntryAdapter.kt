package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Entry
import org.jsoup.Jsoup


class EntryAdapter: ListAdapter<Entry>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        val entry = getItem(position)


        view.txtReferencePreview.visibility = View.GONE
        entry.reference?.let {
            view.txtReferencePreview.visibility = View.VISIBLE
            view.txtReferencePreview.text = it.title
        }

        view.txtEntryPreview.text = getEntryPreview(entry) // TODO: externalize to PreviewService

        view.txtEntryTags.visibility = if(entry.hasTags()) View.VISIBLE else View.GONE

        return view
    }


    private fun getEntryPreview(entry: Entry): String? {
        var entryPreview = Jsoup.parseBodyFragment(entry.abstractString).text()

        if (entryPreview.length < 200) {
            if (entryPreview.length > 0) {
                entryPreview += " "
            }

            entryPreview += Jsoup.parseBodyFragment(entry.content).text()
        }

        return entryPreview
    }

}