package net.dankito.newsreader.model

import java.util.*
import kotlin.collections.LinkedHashMap


open class ArticleSummary(var articles: List<ArticleSummaryItem> = listOf(), var time : Date = Date(),
                               var canLoadMoreItems: Boolean = false, var nextItemsUrl: String? = null, var indexOfAddedItems: Int = -1) {

    fun nextItemsLoaded(nextItemsSummary: ArticleSummary) {
        if(nextItemsSummary.articles.isNotEmpty()) {
            indexOfAddedItems = articles.size + 1
        }
        else {
            indexOfAddedItems = -1
        }

        val mergedArticles = LinkedHashMap<String, ArticleSummaryItem>() // avoids duplicates
        articles.forEach { article ->
            mergedArticles.put(article.url, article)
        }
        nextItemsSummary.articles.forEach { newArticle ->
            mergedArticles.put(newArticle.url, newArticle)
        }
        articles = mergedArticles.values.toList()

        canLoadMoreItems = nextItemsSummary.canLoadMoreItems
        nextItemsUrl = nextItemsSummary.nextItemsUrl
        time = nextItemsSummary.time
    }

}