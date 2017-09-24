package net.dankito.deepthought.android.views

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_tag.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Tag


class TagsPreviewViewHelper {

    fun showTagsPreview(layout: ViewGroup, tags: Collection<Tag>, showButtonRemoveTag: Boolean = false, tagRemovedListener: ((Tag) -> Unit)? = null) {
        layout.removeAllViews()

        val inflater = layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val addMarginManually = layout is FlexboxLayout == false

        val sortedTags = tags.filterNotNull().sortedBy { it.name.toLowerCase() }
        for(i in 0..sortedTags.size - 1) {
            val addSpaceToTheRight = addMarginManually && i < sortedTags.size - 1 // don't add space to last item

            addTagView(inflater, layout, sortedTags[i], addSpaceToTheRight, showButtonRemoveTag, tagRemovedListener)
        }
    }

    private fun addTagView(inflater: LayoutInflater, layout: ViewGroup, tag: Tag, addSpaceToTheRight: Boolean, showButtonRemoveTag: Boolean, tagRemovedListener: ((Tag) -> Unit)?) {
        val tagView = inflater.inflate(R.layout.view_tag, null)
        tagView.txtTagName.text = tag.name

        tagView.btnRemoveTagFromEntry.visibility = if (showButtonRemoveTag) View.VISIBLE else View.GONE
        tagView.btnRemoveTagFromEntry.setOnClickListener { tagRemovedListener?.invoke(tag) }

        layout.addView(tagView)

        // don't know why, but layout_marginRight set in view_tag.xml is ignored by LinearLayout -> set it programmatically
        if(addSpaceToTheRight) {
            setMargin(tagView)
        }
    }

    private fun setMargin(tagView: View) {
        (tagView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
            layoutParams.rightMargin = tagView.context.resources.getDimension(R.dimen.view_tag_margin_right).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.marginEnd = layoutParams.rightMargin
            }
        }
    }

}