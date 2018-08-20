package net.dankito.deepthought.news.article

import net.dankito.utils.AsyncResult
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine

/**
 * ArticleExtractorManager version i only use for my purposes, not publicly available
 */
class ExtendedArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine, configManager: ArticleSummaryExtractorConfigManager)
    : ArticleExtractorManager(seriesService, searchEngine, configManager) {


    override fun extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(item: ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(item, callback)
    }

    override fun extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url: String, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(url, callback)
    }

}