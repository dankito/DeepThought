package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import java.text.DateFormat


private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)


val Source?.preview: String
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


val Source?.seriesAndPublishingDatePreview: String
    get() {
        return getSeriesAndPublishingDatePreview()
    }

fun Source?.getSeriesAndPublishingDatePreview(series: Series? = null): String {
    if(this == null) {
        return ""
    }

    val seriesForPreview = series ?: this.series
    var preview = seriesForPreview?.title ?: ""

    if(publishingDate != null) {
        preview += " " + PublishingDateFormat.format(publishingDate)
    }
    else if(publishingDateString != null) { // publishingDateString may is just a technical date string like 2017.03.27T12:00:00+02:00
        preview += " " + publishingDateString
    }

    return preview.trim()
}


val Source?.previewWithSeriesAndPublishingDate: String
    get() {
        return getPreviewWithSeriesAndPublishingDate()
    }

fun Source?.getPreviewWithSeriesAndPublishingDate(series: Series? = null): String {
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

