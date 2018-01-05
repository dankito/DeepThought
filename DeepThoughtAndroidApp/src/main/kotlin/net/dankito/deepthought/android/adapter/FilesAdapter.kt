package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_file.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.FileLink


class FilesAdapter : ListAdapter<FileLink>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_file, parent, false)

        val file = getItem(position)

        view.txtFileName.text = file.name
        view.txtFilePath.text = file.uriString

        return view
    }

}