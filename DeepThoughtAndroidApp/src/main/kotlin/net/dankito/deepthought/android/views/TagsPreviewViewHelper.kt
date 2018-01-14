package net.dankito.deepthought.android.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_tag.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.setRightMargin
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagAutoCompleteResult
import net.dankito.service.search.SearchEngineBase
import net.dankito.utils.extensions.sorted


class TagsPreviewViewHelper {

    companion object {
        const val TagViewTag = "TagView"
    }


    fun showTagsPreview(layout: ViewGroup, tags: Collection<Tag>, previouslyRecycledTagViews: ArrayList<View>? = null, showButtonRemoveTag: Boolean = false,
                        tagRemovedListener: ((Tag) -> Unit)? = null) {
        val recycledTagViews = recycleTagViews(layout, previouslyRecycledTagViews) // recycle tag views otherwise scrolling in Items RecyclerView would be much too slow

        val inflater = layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val addMarginManually = layout is FlexboxLayout == false

        val sortedTags = tags.sorted()
        for(i in 0..sortedTags.size - 1) {
            val addSpaceToTheRight = addMarginManually && i < sortedTags.size - 1 // don't add space to last item

            addTagView(inflater, layout, recycledTagViews, sortedTags[i], addSpaceToTheRight, showButtonRemoveTag, tagRemovedListener)
        }
    }

    private fun recycleTagViews(layout: ViewGroup, previouslyRecycledTagViews: ArrayList<View>?): ArrayList<View> {
        val recycledTagViews = previouslyRecycledTagViews ?: ArrayList<View>()

        for(i in layout.childCount - 1 downTo 0) {
            val child = layout.getChildAt(i)
            if(isTagView(child)) {
                layout.removeView(child)
                recycledTagViews.add(child)
            }
        }

        return recycledTagViews
    }

    private fun isTagView(child: View?): Boolean {
        return child != null && child.tag == TagViewTag
    }

    private fun addTagView(inflater: LayoutInflater, layout: ViewGroup, recycledTagViews: MutableList<View>, tag: Tag, addSpaceToTheRight: Boolean, showButtonRemoveTag: Boolean,
                           tagRemovedListener: ((Tag) -> Unit)?) {
        val tagView = if(recycledTagViews.size > 0) recycledTagViews.removeAt(0)
                        else inflater.inflate(R.layout.view_tag, null)
        tagView.tag = TagViewTag

        tagView.txtTagName.text = tag.name

        tagView.btnRemoveTagFromEntry.visibility = if (showButtonRemoveTag) View.VISIBLE else View.GONE
        tagView.btnRemoveTagFromEntry.setOnClickListener { tagRemovedListener?.invoke(tag) }

        layout.addView(tagView)

        // don't know why, but layout_marginRight set in view_tag.xml is ignored by LinearLayout -> set it programmatically
        val rightMargin = tagView.context.resources.getDimension(R.dimen.view_tag_margin_right).toInt()
        if(addSpaceToTheRight) {
            (tagView.layoutParams as? ViewGroup.MarginLayoutParams)?.setRightMargin(rightMargin)
        }
    }


    fun autoCompleteEnteredTextForTag(enteredText: String, tag: Tag): TagAutoCompleteResult {
        var lastSearchTermStartIndex = enteredText.lastIndexOf(SearchEngineBase.TagsSearchTermSeparator)
        if(lastSearchTermStartIndex > 0) {
            if(enteredText.substring(lastSearchTermStartIndex + 1).isBlank()) { // if entered text ends with TagsSearchTermSeparator, take text before
                lastSearchTermStartIndex = enteredText.lastIndexOf(SearchEngineBase.TagsSearchTermSeparator, lastSearchTermStartIndex - 1)
            }
        }

        val replacementIndex = lastSearchTermStartIndex + 1
        val enteredTagName = enteredText.substring(replacementIndex)
        val enteredTagNameTrimmedWithoutTagsSeparator = enteredTagName.replace(SearchEngineBase.TagsSearchTermSeparator, "").trim()

        val autoCompletedTagName = (if(lastSearchTermStartIndex <= 0) "" else " ") + tag.name + SearchEngineBase.TagsSearchTermSeparator + " "
        val autoCompletedTagNameTrimmedWithoutTagsSeparator = autoCompletedTagName.replace(SearchEngineBase.TagsSearchTermSeparator, "").trim()

        val autoCompletedText = enteredText.replaceRange(replacementIndex, enteredText.length, autoCompletedTagName)

        return TagAutoCompleteResult(replacementIndex, enteredText, autoCompletedText, enteredTagName, autoCompletedTagName,
                enteredTagNameTrimmedWithoutTagsSeparator, autoCompletedTagNameTrimmedWithoutTagsSeparator, tag)
    }

}