package net.dankito.deepthought.javafx.di

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.NoOpImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine


open class JavaFXInstanceProvider {

    open fun provideArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine, configManager: ArticleSummaryExtractorConfigManager) : ArticleExtractorManager {
        return ArticleExtractorManager(seriesService, searchEngine, configManager)
    }

    open fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return NoOpImplementedArticleSummaryExtractorsManager()
    }

}