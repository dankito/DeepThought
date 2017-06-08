package net.dankito.newsreader.model

import java.util.*


open class ArticleSummary(var articles: List<ArticleSummaryItem> = listOf(), var time : Date = Date(),
                               var canLoadMoreItems: Boolean = false, var nextItemsUrl: String? = null)