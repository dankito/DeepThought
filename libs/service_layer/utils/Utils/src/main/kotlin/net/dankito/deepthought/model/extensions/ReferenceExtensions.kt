package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Reference
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
        if(this == null) {
            return ""
        }

        var preview = series?.title ?: ""

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
        if(this == null) {
            return ""
        }

        var preview = this.preview

        var publisherAndDate = seriesAndPublishingDatePreview

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " " + preview
        }

        return preview
    }

