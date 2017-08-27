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
import net.dankito.deepthought.ui.tags.TagSearchResultState


class TagAdapter(private val presenter: TagsListPresenter) : ListAdapter<Tag>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag, parent, false)

        val tag = getItem(position)

        setTagDisplayText(view, tag)

        setFilterIconDependingOnTagState(tag, view.imgFilter)
        view.imgFilter.setOnClickListener { presenter.toggleFilterTag(tag) }

        setBackgroundColor(view, tag)

        return view
    }

    private fun setTagDisplayText(view: View, tag: Tag) {
        view.txtTagDisplayText.visibility = if (tag == null) View.INVISIBLE else View.VISIBLE

        if(tag != null) {
            if(presenter.isTagFilterApplied()) {
                view.txtTagDisplayText.text = "${tag.name} (${presenter.getCountEntriesForFilteredTag(tag)} / ${tag.countEntries})"
            }
            else {
                view.txtTagDisplayText.text = tag.displayText
            }
        }
    }


    private fun setFilterIconDependingOnTagState(tag: Tag, imgFilter: ImageView) {
        if(tag is CalculatedTag || tag == null) {
            imgFilter.visibility = View.INVISIBLE
        }
        else {
            imgFilter.visibility = View.VISIBLE

            when(presenter.isTagFiltered(tag)) {
                true -> imgFilter.setImageResource(R.drawable.filter)
                false -> imgFilter.setImageResource(R.drawable.filter_disabled)
            }
        }
    }

    private fun setBackgroundColor(view: View, tag: Tag) {
        val state = presenter.getTagSearchResultState(tag)

        view.setBackgroundResource(getColorForState(state))
    }

    private fun getColorForState(state: TagSearchResultState): Int {
        when(state) {
            TagSearchResultState.EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT -> return R.color.tag_state_exact_or_single_match_but_not_of_last_result
            TagSearchResultState.MATCH_BUT_NOT_OF_LAST_RESULT -> return R.color.tag_state_match_but_not_of_last_result
            TagSearchResultState.EXACT_MATCH_OF_LAST_RESULT -> return R.color.tag_state_exact_match_of_last_result
            TagSearchResultState.SINGLE_MATCH_OF_LAST_RESULT -> return R.color.tag_state_single_match_of_last_result
            else -> return R.color.tag_state_default
        }
    }

}