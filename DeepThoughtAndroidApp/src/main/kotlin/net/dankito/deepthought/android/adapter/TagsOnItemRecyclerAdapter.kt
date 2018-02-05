package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.TagsOnItemViewHolder
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnItemListPresenter
import net.dankito.deepthought.ui.tags.TagSearchResultState
import java.util.*


class TagsOnItemRecyclerAdapter(private val presenter: TagsOnItemListPresenter, val listener: (TagChange, Tag, MutableCollection<Tag>) -> Unit)
    : ListRecyclerSwipeAdapter<Tag, TagsOnItemViewHolder>() {

    enum class TagChange {
        Added,
        Removed
    }


    var tagsOnItem: MutableSet<Tag> = mutableSetOf()

    var deleteTagListener: ((Tag) -> Unit)? = null


    override fun getSwipeLayoutResourceId(position: Int) = R.id.tagOnItemSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TagsOnItemViewHolder {
        val context = parent?.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_tag_on_item, parent, false)

        val viewHolder = TagsOnItemViewHolder(itemView)
        viewHolder.vwIsTagOnItem.setEntityNameTextSizeToHeader1TextSize()

        (itemView as? SwipeLayout)?.addRevealListener(itemView.id) { _, _, _, _ -> viewHolder.lastItemSwipeTime = Date() }

        viewHolderCreated(viewHolder)
        return viewHolder
    }


    override fun bindViewForNullValue(viewHolder: TagsOnItemViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.vwIsTagOnItem.showState("", false)
        setBackgroundForDefaultState(viewHolder.itemView)
    }

    override fun bindItemToView(viewHolder: TagsOnItemViewHolder, item: Tag) {
        val isAddedToItem = tagsOnItem.contains(item)

        viewHolder.vwIsTagOnItem.showState(item.displayText, isAddedToItem)

        setBackgroundColor(viewHolder.itemView, item)

        viewHolder.itemView.setOnClickListener { itemClicked(viewHolder, item) }
    }

    override fun setupSwipeView(viewHolder: TagsOnItemViewHolder, item: Tag) {
        viewHolder.btnEditTag.setOnClickListener {
            presenter.editTag(item)
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteTag.setOnClickListener {
            deleteTagListener?.invoke(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun itemClicked(viewHolder: TagsOnItemViewHolder, tag: Tag) {
        val lastSwipeTime = viewHolder.lastItemSwipeTime

        // a swipe on an item also triggers onClickListener -> filter out swipes before calling toggleTagOnItemOnUIThread() as otherwise swipe layout would get closed
        // immediately again -> wouldn't be possible to activate list item actions anymore
        if(lastSwipeTime == null || Date().time - lastSwipeTime.time > 200) {
            toggleTagOnItemOnUIThread(tag)
        }
    }

    private fun toggleTagOnItemOnUIThread(tag: Tag) {
        val tagChange: TagChange

        if(tagsOnItem.contains(tag)) {
            tagsOnItem.remove(tag)
            tagChange = TagChange.Removed
        }
        else {
            tagsOnItem.add(tag)
            tagChange = TagChange.Added
        }

        notifyDataSetChanged()

        callListener(tagChange, tag)
    }

    private fun callListener(tagChange: TagChange, tag: Tag) {
        listener(tagChange, tag, tagsOnItem)
    }


    private fun setBackgroundColor(view: View, tag: Tag) {
        val state = presenter.getTagSearchResultState(tag)

        view.setBackgroundResource(getColorForState(state))
    }

    private fun setBackgroundForDefaultState(view: View) {
        view.setBackgroundResource(getDefaultBackgroundColor())
    }

    private fun getColorForState(state: TagSearchResultState): Int {
        when(state) {
            TagSearchResultState.EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT -> return R.color.tag_state_exact_or_single_match_but_not_of_last_result
            TagSearchResultState.EXACT_MATCH_OF_LAST_RESULT -> return R.color.tag_state_exact_or_single_match_of_last_result
            TagSearchResultState.SINGLE_MATCH_OF_LAST_RESULT -> return R.color.tag_state_exact_or_single_match_of_last_result
            else -> return getDefaultBackgroundColor()
        }
    }

    private fun getDefaultBackgroundColor() = R.color.tag_state_default

}