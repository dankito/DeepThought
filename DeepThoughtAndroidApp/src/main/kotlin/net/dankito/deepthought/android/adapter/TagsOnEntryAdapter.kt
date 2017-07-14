package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_tag_on_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Tag


class TagsOnEntryAdapter(val listener: (MutableList<Tag>) -> Unit) : ListAdapter<Tag>() {

    var tagsOnEntry: MutableList<Tag> = mutableListOf()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag_on_entry, parent, false)

        val tag = getItem(position)

        view.chktxtvwTag.text = tag.displayText

        view.chktxtvwTag.isChecked = tagsOnEntry.contains(tag)

        view.chktxtvwTag.setOnClickListener { toggleTagOnEntryOnUIThread(position) }

        return view
    }


    private fun toggleTagOnEntryOnUIThread(position: Int) {
        val tag = getItem(position)

        if(tagsOnEntry.contains(tag)) {
            tagsOnEntry.remove(tag)
        }
        else {
            tagsOnEntry.add(tag)
//            Collections.sort(tagsOnEntry) // TODO
        }

        notifyDataSetChanged()

        callListener()
    }

    private fun callListener() {
        listener(tagsOnEntry)
    }

}