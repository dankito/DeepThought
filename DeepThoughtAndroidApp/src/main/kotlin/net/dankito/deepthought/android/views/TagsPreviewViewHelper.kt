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
import net.dankito.utils.extensions.sortedByStrings


class TagsPreviewViewHelper {

    fun showTagsPreview(layout: ViewGroup, tags: Collection<Tag>, previouslyRecycledTagViews: ArrayList<View>? = null, showButtonRemoveTag: Boolean = false,
                        tagRemovedListener: ((Tag) -> Unit)? = null) {
        val recycledTagViews = recycleTagViews(layout, previouslyRecycledTagViews) // recycle tag views otherwise scrolling in Items RecyclerView would be much too slow

        val inflater = layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val addMarginManually = layout is FlexboxLayout == false

        val sortedTags = tags.filterNotNull().sortedByStrings { it.name }
        for(i in 0..sortedTags.size - 1) {
            val addSpaceToTheRight = addMarginManually && i < sortedTags.size - 1 // don't add space to last item

            addTagView(inflater, layout, recycledTagViews, sortedTags[i], addSpaceToTheRight, showButtonRemoveTag, tagRemovedListener)
        }
    }

    private fun recycleTagViews(layout: ViewGroup, previouslyRecycledTagViews: ArrayList<View>?): ArrayList<View> {
        val recycledTagViews = previouslyRecycledTagViews ?: ArrayList<View>()

        for (i in 0..layout.childCount - 1) {
            recycledTagViews.add(layout.getChildAt(i))
        }

        layout.removeAllViews()

        return recycledTagViews
    }

    private fun addTagView(inflater: LayoutInflater, layout: ViewGroup, recycledTagViews: MutableList<View>, tag: Tag, addSpaceToTheRight: Boolean, showButtonRemoveTag: Boolean,
                           tagRemovedListener: ((Tag) -> Unit)?) {
        val tagView = if(recycledTagViews.size > 0) recycledTagViews.removeAt(0)
                        else inflater.inflate(R.layout.view_tag, null)

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

}