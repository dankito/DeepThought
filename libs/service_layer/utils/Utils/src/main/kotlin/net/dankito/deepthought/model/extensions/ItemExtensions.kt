package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.utils.extensions.toSortedString


const val SeriesAndPublishingDateAndItemPreviewSeparator = " | "

const val MaxPreviewLength = 400


val Item.summaryPlainText: String
    get() {
        return this.summary.getPlainTextForHtml()
    }


val Item.contentPlainText: String
    get() {
        return this.content.getPlainTextForHtml()
    }


val Item.itemPreview: String
    get()  {
        val includeItemSummary = this.sourcePreview.isNullOrBlank() == false

        return getItemPreview(includeItemSummary)
    }


fun Item.getItemPreview(includeItemSummary: Boolean): String {
    var preview = if(includeItemSummary) this.summaryPlainText else ""

    if(preview.length < MaxPreviewLength) {
        if(preview.isNotEmpty()) {
            preview += " - "
        }

        preview += this.contentPlainText
    }

    if(preview.length > MaxPreviewLength) {
        preview = preview.substring(0, MaxPreviewLength) + "..."
    }

    return preview
}


fun Item.getItemPreviewWithSeriesAndPublishingDate(source: Source?, series: Series? = null): String {
    var preview = this.preview

    val seriesAndPublishingDate = source.getSeriesAndPublishingDatePreview(series)
    if(seriesAndPublishingDate.isNullOrBlank() == false) {
        if(preview.isNullOrBlank()) {
            preview = seriesAndPublishingDate
        }
        else {
            preview = seriesAndPublishingDate + SeriesAndPublishingDateAndItemPreviewSeparator + preview
        }
    }

    return preview
}


val Item.sourcePreview: String
    get() {
        this.source?.let { source ->
            var preview = source.title

            if(source.subTitle.isNullOrBlank() == false) {
                preview = source.subTitle + ": " + preview
            }

            return preview
        }

        return ""
    }


val Item.tagsPreview: String
    get() {
        return this.tags.toSortedString()
    }