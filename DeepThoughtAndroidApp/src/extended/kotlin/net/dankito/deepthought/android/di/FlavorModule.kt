package net.dankito.deepthought.android.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.android.service.reporting.NoOpCrashReporter
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.ExtendedArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.ImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import javax.inject.Singleton


@Module
class FlavorModule {

    @Provides
    @Singleton
    fun provideArticleSummaryPresenter(entryPersister: EntryPersister, readLaterArticleService: ReadLaterArticleService, articleExtractorManager: ArticleExtractorManager,
                                       router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService) : ArticleSummaryPresenter {
        return ExtendedArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)
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