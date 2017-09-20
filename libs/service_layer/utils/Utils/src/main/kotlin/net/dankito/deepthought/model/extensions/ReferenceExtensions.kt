package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import java.text.DateFormat


private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)


val Reference?.preview: String
    get() {
        if(this == null) {
            return ""
        }

        var preview = title

        if(subTitle.isNullOrBlank() == false) {
            preview = subTitle + ": " + preview
        }

        return preview
    }


val Reference?.seriesAndPublishingDatePreview: String
    get() {
        return getSeriesAndPublishingDatePreview()
    }

fun Reference?.getSeriesAndPublishingDatePreview(series: Series? = null): String {
    if(this == null) {
        return ""
    }

    val seriesForPreview = series ?: this.series
    var preview = seriesForPreview?.title ?: ""

    if(publishingDateString != null) {
        preview += " " + publishingDateString
    }
    else if(publishingDate != null) {
        preview += " " + PublishingDateFormat.format(publishingDate)
    }

    return preview.trim()
}


val Reference?.previewWithSeriesAndPublishingDate: String
    get() {
        return getPreviewWithSeriesAndPublishingDate()
    }

fun Reference?.getPreviewWithSeriesAndPublishingDate(series: Series? = null): String {
    if(this == null) {
        return ""
    }

    var preview = this.preview

    val publisherAndDate = getSeriesAndPublishingDatePreview(series)

    if(publisherAndDate.isNullOrBlank() == false) {
        preview = publisherAndDate.trim() + " " + preview
    }

    return preview
}

