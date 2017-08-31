package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_reference.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.Reference


class ReferencesAdapter() : ListAdapter<Reference>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        val reference = getItem(position)

        view.txtReferencePreview.text = reference.preview

        return view
    }

}