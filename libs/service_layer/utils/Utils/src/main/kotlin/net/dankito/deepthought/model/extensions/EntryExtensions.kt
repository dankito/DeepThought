package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series


const val SeriesAndPublishingDateAndEntryPreviewSeparator = " | "

private const val MaxPreviewLength = 400


val Entry.abstractPlainText: String
    get() {
        return this.abstractString.getPlainTextForHtml()
    }


val Entry.contentPlainText: String
    get() {
        return this.content.getPlainTextForHtml()
    }


val Entry.entryPreview: String
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


fun Entry.getEntryPreviewWithSeriesAndPublishingDate(reference: Reference?, series: Series? = null): String {
    var preview = this.preview

    val seriesAndPublishingDate = reference.getSeriesAndPublishingDatePreview(series)
    if(seriesAndPublishingDate.isNullOrBlank() == false) {
        preview = seriesAndPublishingDate + SeriesAndPublishingDateAndEntryPreviewSeparator + preview
    }

    return preview
}


val Entry.referencePreview: String
    get() {
        this.reference?.let { reference ->
            var preview = reference.title

            if(reference.subTitle.isNullOrBlank() == false) {
                preview = reference.subTitle + ": " + preview
            }

            return preview
        }

        return ""
    }


val Entry.tagsPreview: String
    get() {
        return this.tags.filterNotNull().sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }