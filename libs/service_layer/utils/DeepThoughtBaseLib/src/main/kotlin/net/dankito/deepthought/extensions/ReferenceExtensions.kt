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

        var publisherAndDate = series?.title ?: ""

        publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

        if(publisherAndDate.isNullOrBlank() == false) {
            preview = publisherAndDate.trim() + " " + preview
        }

        return preview
    }

