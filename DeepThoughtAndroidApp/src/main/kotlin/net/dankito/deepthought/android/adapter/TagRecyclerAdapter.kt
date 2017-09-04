package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.TagViewHolder
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.tags.TagSearchResultState


class TagRecyclerAdapter(private val presenter: TagsListPresenter): ListRecyclerSwipeAdapter<Tag, TagViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = 0


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TagViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag, parent, false)

        return TagViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: TagViewHolder, position: Int) {
        val tag = getItem(position)

        if(tag == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindTagToView(viewHolder, tag)
            itemBound(viewHolder, tag, position)
        }
    }

    private fun bindViewForNullValue(viewHolder: TagViewHolder) {
        viewHolder.txtTagDisplayText.visibility = View.INVISIBLE

        viewHolder.imgFilter.visibility = View.INVISIBLE

        viewHolder.itemView.setBackgroundResource(getDefaultBackgroundColor())
    }

    private fun bindTagToView(viewHolder: TagViewHolder, tag: Tag) {
        viewHolder.txtTagDisplayText.visibility = View.VISIBLE

        if(presenter.isTagFilterApplied()) {
            viewHolder.txtTagDisplayText.text = "${tag.name} (${presenter.getCountEntriesForFilteredTag(tag)} / ${tag.countEntries})"
        }
        else {
            viewHolder.txtTagDisplayText.text = tag.displayText
        }

        setFilterIconDependingOnTagState(tag, viewHolder.imgFilter)
        viewHolder.imgFilter.setOnClickListener { presenter.toggleFilterTag(tag) }

        setBackgroundColor(viewHolder.itemView, tag)
    }


    private fun setFilterIconDependingOnTagState(tag: Tag, imgFilter: ImageView) {
        if(tag is CalculatedTag) {
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
            else -> return getDefaultBackgroundColor()
        }
    }

    private fun getDefaultBackgroundColor() = R.color.tag_state_default

}