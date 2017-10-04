package net.dankito.deepthought.javafx.di

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.javafx.dialogs.articlesummary.presenter.ExtendedArticleSummaryPresenter
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.newsreader.summary.ImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService

open class JavaFXExtendedInstanceProvider : JavaFXInstanceProvider() {

    override fun provideArticleSummaryPresenter(entryPersister: EntryPersister, readLaterArticleService: ReadLaterArticleService, articleExtractorManager: ArticleExtractorManager,
                                            router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService) : ArticleSummaryPresenter {
        return ExtendedArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService)
    }

    override fun provideImplementedArticleSummaryExtractorsManager(webClient: IWebClient) : IImplementedArticleSummaryExtractorsManager {
        return ImplementedArticleSummaryExtractorsManager(webClient)
    }

}