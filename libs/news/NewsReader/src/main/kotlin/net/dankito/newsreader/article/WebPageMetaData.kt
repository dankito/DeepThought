package net.dankito.newsreader.article

import java.util.*
import kotlin.collections.HashSet


data class WebPageMetaData(var title: String? = null, var description: String? = null,
                           var siteName: String? = null, var previewImageUrl: String? = null,
                           var author: String? = null, var keywords: MutableSet<String> = HashSet(),
                           var publishingDateString: String? = null, var publishingDate: Date? = null)