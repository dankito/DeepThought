package net.dankito.newsreader.model

import java.util.*
import kotlin.collections.LinkedHashMap


open class ArticleSummary(var articles: List<ArticleSummaryItem> = listOf(), var time : Date = Date(),
                               var canLoadMoreItems: Boolean = false, var nextItemsUrl: String? = null, var indexOfAddedItems: Int = -1) {

    fun removeDuplicateArticles() {
        val uniqueArticles = LinkedHashMap<String, ArticleSummaryItem>()

        articles.forEach { article ->
            if(uniqueArticles.containsKey(article.url) == false) { // TODO: check which article version has more information
                uniqueArticles.put(article.url, article)
            }
        }

        articles = uniqueArticles.values.toList()
    }


    fun nextItemsLoaded(nextItemsSummary: ArticleSummary, removeDuplicates: Boolean = false) {
        if(nextItemsSummary.articles.isNotEmpty()) {
            indexOfAddedItems = articles.size + 1
        }
        else {
            indexOfAddedItems = -1
        }

        if(removeDuplicates) {
            mergeArticlesRemoveDuplicates(nextItemsSummary)
        }
        else {
            mergeArticles(nextItemsSummary)
        }

        canLoadMoreItems = nextItemsSummary.canLoadMoreItems
        nextItemsUrl = nextItemsSummary.nextItemsUrl
        time = nextItemsSummary.time
    }

    private fun mergeArticles(nextItemsSummary: ArticleSummary) {
        val mergedArticles = ArrayList(articles)
        mergedArticles.addAll(nextItemsSummary.articles)
        articles = mergedArticles
    }

    private fun mergeArticlesRemoveDuplicates(nextItemsSummary: ArticleSummary) {
        val mergedArticles = LinkedHashMap<String, ArticleSummaryItem>()

        articles.forEach { article ->
            mergedArticles.put(article.url, article)
        }
        nextItemsSummary.articles.forEach { newArticle ->
            mergedArticles.put(newArticle.url, newArticle)
        }

        articles = mergedArticles.values.toList()
    }

}