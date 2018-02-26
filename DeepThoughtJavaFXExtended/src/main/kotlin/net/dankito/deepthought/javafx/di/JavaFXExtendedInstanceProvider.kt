package net.dankito.deepthought.javafx.di

import net.dankito.util.web.IWebClient
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.article.ExtendedArticleExtractorManager
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.ImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine


open class JavaFXExtendedInstanceProvider : JavaFXInstanceProvider() {

    override fun provideArticleExtractorManager(seriesService: SeriesService, searchEngine: ISearchEngine) : ArticleExtractorManager {
        return ExtendedArticleExtractorManager(seriesService, searchEngine)
    }

    override fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return ImplementedArticleSummaryExtractorsManager(webClient)
    }

}