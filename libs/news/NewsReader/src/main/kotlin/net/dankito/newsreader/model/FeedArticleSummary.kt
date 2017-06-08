package net.dankito.newsreader.model

import java.util.*


class FeedArticleSummary(var title : String? = null, var siteUrl : String? = null,
                              var imageUrl : String? = null, var publishedDate : Date? = null)
    : ArticleSummary()