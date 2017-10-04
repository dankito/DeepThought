package net.dankito.deepthought.javafx.dialogs.articlesummary.presenter

import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService


class ExtendedArticleSummaryPresenter(entryPersister: EntryPersister, readLaterArticleService: ReadLaterArticleService, articleExtractorManager: ArticleExtractorManager,
                                      router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService)
    : ArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService) {


    override fun getAndShowArticle(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { showArticle(it) }
        }
    }

}