package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_tag.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Tag


class TagAdapter: ListAdapter<Tag>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag, parent, false)

        val tag = getItem(position)

        view.txtTagDisplayText.text = tag.displayText

        return view
    }

}