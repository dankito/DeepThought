package net.dankito.deepthought.android.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.android.service.reporting.NoOpCrashReporter
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.article.ExtendedArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.ImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.web.client.IWebClient
import javax.inject.Singleton


@Module
class FlavorModule {

    @Provides
    @Singleton
    fun provideArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine, configManager: ArticleSummaryExtractorConfigManager) : ArticleExtractorManager {
        return ExtendedArticleExtractorManager(seriesService, searchEngine, configManager)
    }


    @Provides
    @Singleton
    fun provideCrashReporter() : ICrashReporter {
        return NoOpCrashReporter()
    }

    @Provides
    @Singleton
    fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return ImplementedArticleSummaryExtractorsManager(webClient)
    }

}