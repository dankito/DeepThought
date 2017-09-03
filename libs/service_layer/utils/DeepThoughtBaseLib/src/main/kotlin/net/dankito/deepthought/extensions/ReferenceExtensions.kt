package net.dankito.deepthought.extensions

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

val Reference?.previewWithSeriesAndPublishingDate: String
    get() {
        if(this == null) {
            return ""
        }

        var preview = this.preview

        var publisherAndDate = series?.title ?: ""

        publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " " + preview
        }

        return preview
    }

