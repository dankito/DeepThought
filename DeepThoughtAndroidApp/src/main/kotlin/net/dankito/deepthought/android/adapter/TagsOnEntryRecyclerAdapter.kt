package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.TagsOnEntryViewHolder
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagSearchResultState


class TagsOnEntryRecyclerAdapter(private val presenter: TagsOnEntryListPresenter, val listener: (MutableList<Tag>) -> Unit) : ListRecyclerSwipeAdapter<Tag, TagsOnEntryViewHolder>() {

    var tagsOnEntry: MutableList<Tag> = mutableListOf()


    override fun getSwipeLayoutResourceId(position: Int) = 0


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TagsOnEntryViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tag_on_entry, parent, false)

        return TagsOnEntryViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: TagsOnEntryViewHolder, position: Int) {
        val tag = getItem(position)

        if(tag == null) {
            viewHolder.chktxtvwTag.visibility = View.INVISIBLE
        }
        else {
            viewHolder.chktxtvwTag.visibility = View.VISIBLE

            viewHolder.chktxtvwTag.text = tag.displayText
            viewHolder.chktxtvwTag.isChecked = tagsOnEntry.contains(tag)

            viewHolder.chktxtvwTag.setOnClickListener { toggleTagOnEntryOnUIThread(position) }

            itemBound(viewHolder, tag, position)
        }

        setBackgroundColor(viewHolder.itemView, tag)
    }


    private fun toggleTagOnEntryOnUIThread(position: Int) {
        val tag = getItem(position)

        if(tagsOnEntry.contains(tag)) {
            tagsOnEntry.remove(tag)
        }
        else {
            tagsOnEntry.add(tag)
        }

        notifyDataSetChanged()

        callListener()
    }

    private fun callListener() {
        listener(tagsOnEntry)
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