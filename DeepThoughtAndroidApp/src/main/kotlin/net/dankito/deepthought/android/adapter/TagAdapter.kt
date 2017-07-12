package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_tag.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsListPresenter


class TagAdapter(private val presenter: TagsListPresenter) : ListAdapter<Tag>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag, parent, false)

        val tag = getItem(position)

        view.txtTagDisplayText.text = tag.displayText

        setFilterIconDependingOnTagState(tag, view.imgFilter)
        view.imgFilter.setOnClickListener { presenter.toggleFilterTag(tag) }

        return view
    }


    private fun setFilterIconDependingOnTagState(tag: Tag, imgFilter: ImageView) {
        if(tag is CalculatedTag) {
            imgFilter.visibility = View.INVISIBLE
        }
        else {
            imgFilter.visibility = View.VISIBLE
        }

        when(presenter.isTagFiltered(tag)) {
            true -> imgFilter.setImageResource(R.drawable.filter)
            false -> imgFilter.setImageResource(R.drawable.filter_disabled)
        }
    }

}