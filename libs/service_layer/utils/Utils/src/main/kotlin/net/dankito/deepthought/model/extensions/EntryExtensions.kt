package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.utils.extensions.sortedByStrings


const val SeriesAndPublishingDateAndEntryPreviewSeparator = " | "

const val MaxPreviewLength = 400


val Item.abstractPlainText: String
    get() {
        return this.summary.getPlainTextForHtml()
    }


val Item.contentPlainText: String
    get() {
        return this.content.getPlainTextForHtml()
    }


val Item.entryPreview: String
    get()  {
        val includeItemSummary = this.referencePreview.isNullOrBlank() == false
        var preview = if(includeItemSummary) this.abstractPlainText else ""

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


fun Item.getEntryPreviewWithSeriesAndPublishingDate(source: Source?, series: Series? = null): String {
    var preview = this.preview

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
        return this.tags.filterNotNull().sortedByStrings { it.name.toLowerCase() }.joinToString { it.name }
    }