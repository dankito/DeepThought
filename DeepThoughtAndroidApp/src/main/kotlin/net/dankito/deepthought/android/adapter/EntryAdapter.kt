package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import org.jsoup.Jsoup
import java.text.DateFormat


class EntryAdapter: ListAdapter<Entry>() {

    companion object {
        private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_entry, parent, false)

        val entry = getItem(position)


        view.txtReferencePreview.visibility = View.GONE
        entry.reference?.let {
            view.txtReferencePreview.visibility = View.VISIBLE
            view.txtReferencePreview.text = getReferencePreview(it)
        }

        view.txtEntryPreview.text = getEntryPreview(entry) // TODO: externalize to PreviewService

        view.txtEntryTags.visibility = if(entry.hasTags()) View.VISIBLE else View.GONE

        return view
    }


    private fun getEntryPreview(entry: Entry): String? {
        var preview = Jsoup.parseBodyFragment(entry.abstractString).text()

        if (preview.length < 200) {
            if (preview.length > 0) {
                preview += " "
            }

            preview += Jsoup.parseBodyFragment(entry.content).text()
        }

        return preview
    }

    private fun getReferencePreview(reference: Reference): String? {
        var preview = reference.title

        var publisherAndDate = reference.series ?: ""

        reference.publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " - " + preview
        }

        return preview
    }

}