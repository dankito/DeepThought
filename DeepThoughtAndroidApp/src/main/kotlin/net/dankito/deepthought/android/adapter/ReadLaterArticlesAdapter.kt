package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_read_later_article.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.preview


class ReadLaterArticlesAdapter : ListAdapter<ReadLaterArticle>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val extractionResult = getItem(position).entryExtractionResult

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_read_later_article, parent, false)

        val referencePreview = extractionResult.reference?.preview
        view.txtTitle.visibility = if(referencePreview?.isBlank() ?: true) { GONE } else { VISIBLE }
        view.txtTitle.text = referencePreview

        view.txtSummary.text = extractionResult.entry.preview

        Picasso.with(view?.context)
                .load(extractionResult.reference?.previewImageUrl)
                .into(view.imgPreviewImage)

        return view
    }

}