package net.dankito.deepthought.android.service

import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class ExtractArticleHandler {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)
    }


    fun extractAndShowArticleUserDidSeeBefore(url: String) {
        articleExtractorManager.extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditEntryView(it) }
            it.error?.let { showCouldNotExtractItemErrorMessage(it, url) }
        }
    }

    fun extractAndShowArticleUserDidNotSeeBefore(url: String) {
        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditEntryView(it) }
            it.error?.let { showCouldNotExtractItemErrorMessage(it, url) }
        }
    }

    private fun showCouldNotExtractItemErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(dialogService.getLocalization().getLocalizedString("alert.message.could.not.extract.item.from.url", articleUrl), exception = error)
    }

}