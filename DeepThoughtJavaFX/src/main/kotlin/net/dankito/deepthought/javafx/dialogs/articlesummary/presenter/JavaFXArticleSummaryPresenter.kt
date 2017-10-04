package net.dankito.deepthought.javafx.dialogs.articlesummary.presenter

import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService


open class JavaFXArticleSummaryPresenter(entryPersister: EntryPersister, readLaterArticleService: ReadLaterArticleService,
                                    articleExtractorManager: ArticleExtractorManager, router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService)
    : ArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService) {



}