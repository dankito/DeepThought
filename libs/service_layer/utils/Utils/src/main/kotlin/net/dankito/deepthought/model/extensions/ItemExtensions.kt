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
    val preview = StringBuilder( if(includeItemSummary) this.summaryPlainText else "" )

    if(preview.length < MaxPreviewLength) {
        if(preview.isNotEmpty()) {
            preview.append(" - ")
        }

        preview.append(this.contentPlainText)
    }

    if(preview.length > MaxPreviewLength) {
        preview.setLength(MaxPreviewLength)
        preview.append("...")
    }

    return preview.toString()
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
        val preview = source.preview

        return addIndicationIfSet(preview)
    }

val Item.sourcePreviewWithSeriesAndPublishingDate: String
    get() {
        val preview = source.previewWithSeriesAndPublishingDate

        return addIndicationIfSet(preview)
    }

private fun Item.addIndicationIfSet(preview: String): String {
    if (this.hasIndication()) {
        if(preview.isNullOrBlank()) {
            return indication
        }

        return "$preview $indication"
    }

    return preview
}


val Item.sourcePreviewOrSummary: String
    get() {
        val sourcePreview = this.sourcePreview

        if(sourcePreview.isNullOrBlank() && this.summary.isNullOrBlank() == false) {
            return this.summaryPlainText
        }

        return sourcePreview
    }


val Item.tagsPreview: String
    get() {
        return this.tags.toSortedString()
    }