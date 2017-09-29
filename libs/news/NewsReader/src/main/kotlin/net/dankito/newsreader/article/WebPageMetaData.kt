package net.dankito.newsreader.article

import java.util.*


data class WebPageMetaData(var title: String? = null, var description: String? = null,
                           var siteName: String? = null, var previewImageUrl: String? = null,
                           var publishingDateString: String? = null, var publishingDate: Date? = null)