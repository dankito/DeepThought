package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series


const val SeriesAndPublishingDateAndEntryPreviewSeparator = " | "

private const val MaxPreviewLength = 400


val Item.abstractPlainText: String
    get() {
        return this.summary.getPlainTextForHtml()
    }


val Item.contentPlainText: String
    get() {
        return this.content.getPlainTextForHtml()
    }


val Item.entryPreview: String
    get() {
        var preview = this.abstractPlainText

        if(preview.length < MaxPreviewLength) {
            if(preview.isNotEmpty()) {
                preview += " "
            }

            preview += this.contentPlainText
        }

        if(preview.length > MaxPreviewLength) {
            preview = preview.substring(0, MaxPreviewLength) + "..."
        }

        return preview
    }


fun Item.getEntryPreviewWithSeriesAndPublishingDate(source: Source?, series: Series? = null): String {
    var preview = this.entryPreview

    val seriesAndPublishingDate = source.getSeriesAndPublishingDatePreview(series)
    if(seriesAndPublishingDate.isNullOrBlank() == false) {
        if(preview.isNullOrBlank()) {
            preview = seriesAndPublishingDate
        }
        else {
            preview = seriesAndPublishingDate + SeriesAndPublishingDateAndEntryPreviewSeparator + preview
        }
    }

    return preview
}


val Item.referencePreview: String
    get() {
        this.source?.let { reference ->
            var preview = reference.title

            if(reference.subTitle.isNullOrBlank() == false) {
                preview = reference.subTitle + ": " + preview
            }

            return preview
        }

        return ""
    }


val Item.tagsPreview: String
    get() {
        return this.tags.filterNotNull().sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }