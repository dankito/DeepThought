package net.dankito.deepthought.javafx.di

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.NoOpImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine


open class JavaFXInstanceProvider {

    open fun provideArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine) : ArticleExtractorManager {
        return ArticleExtractorManager(seriesService, searchEngine)
    }

    open fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return NoOpImplementedArticleSummaryExtractorsManager()
    }

}