package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_article_summary_item.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.ReadLaterArticle


class ReadLaterArticlesAdapter : ListAdapter<ReadLaterArticle>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val extractionResult = getItem(position).entryExtractionResult

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_item, parent, false)

        val referencePreview = extractionResult.reference?.preview
        view.txtTitle.visibility = if(referencePreview?.isBlank() ?: true) { GONE } else { VISIBLE }
        view.txtTitle.text = referencePreview

        view.txtSummary.text = extractionResult.entry.entryPreview

        Picasso.with(view?.context)
                .load(extractionResult.entry.previewImageUrl)
                .into(view.imgPreviewImage)

        return view
    }

}