package net.dankito.deepthought.android.views

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_tag.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Tag


class TagsPreviewViewHelper {

    fun showTagsPreview(layout: ViewGroup, tags: Collection<Tag>) {
        layout.removeAllViews()

        val inflater = layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val sortedTags = tags.filterNotNull().sortedBy { it.name.toLowerCase() }
        for(i in 0..sortedTags.size - 1) {
            val tag = sortedTags[i]

            val tagView = inflater.inflate(R.layout.view_tag, null)
            tagView.txtTagName.text = tag.name

            layout.addView(tagView)

            // don't know why, but layout_marginRight set in view_tag.xml is ignored by FlexboxLayout -> set it programmatically
            if(i < sortedTags.size - 1) {
                setMargin(tagView)
            }
        }
    }

    private fun setMargin(tagView: View) {
        (tagView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
            layoutParams.rightMargin = tagView.context.resources.getDimension(R.dimen.view_tag_text_margin_right).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.marginEnd = layoutParams.rightMargin
            }
        }
    }

}