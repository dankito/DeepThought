package net.dankito.newsreader.model

import java.util.*
import kotlin.collections.ArrayList


open class ArticleSummary(var articles: List<ArticleSummaryItem> = listOf(), var time : Date = Date(),
                               var canLoadMoreItems: Boolean = false, var nextItemsUrl: String? = null, var indexOfAddedItems: Int = -1) {

    fun nextItemsLoaded(nextItemsSummary: ArticleSummary) {
        if(nextItemsSummary.articles.isNotEmpty()) {
            indexOfAddedItems = articles.size + 1
        }
        else {
            indexOfAddedItems = -1
        }

        val mergedArticles = ArrayList(articles)
        mergedArticles.addAll(nextItemsSummary.articles)
        articles = mergedArticles

        canLoadMoreItems = nextItemsSummary.canLoadMoreItems
        nextItemsUrl = nextItemsSummary.nextItemsUrl
        time = nextItemsSummary.time
    }

}