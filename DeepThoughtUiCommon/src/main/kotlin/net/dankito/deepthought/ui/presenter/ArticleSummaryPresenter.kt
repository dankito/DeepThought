package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.extensions.extractor
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import java.util.concurrent.CountDownLatch
import javax.inject.Inject


class ArticleSummaryPresenter(protected val itemPersister: ItemPersister, protected val readLaterArticleService: ReadLaterArticleService, protected val articleExtractorManager: ArticleExtractorManager,
                                   protected val router: IRouter, private val clipboardService: IClipboardService, protected val dialogService: IDialogService) {


    @Inject
    protected lateinit var localization: Localization

    @Inject
    protected lateinit var threadPool: IThreadPool


    var lastLoadedSummary: ArticleSummary? = null


    init {
        CommonComponent.component.inject(this)
    }


    fun extractArticlesSummary(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.extractSummaryAsync {
            retrievedArticleSummary(it, extractorConfig, callback)
        }
    }

    fun loadMoreItems(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        lastLoadedSummary?.let { articleSummary ->
            extractorConfig?.extractor?.loadMoreItemsAsync(articleSummary) {
                retrievedArticleSummary(it, extractorConfig, callback)
            }
        }
    }

    private fun retrievedArticleSummary(result: AsyncResult<out ArticleSummary>, extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        result.result?.let { summary ->
            retrievedArticleSummary(summary, extractorConfig)
        }

        result.error?.let { error -> showError("alert.message.could.not.load.article.summary", error) }

        callback(result)
    }

    fun retrievedArticleSummary(summary: ArticleSummary, extractorConfig: ArticleSummaryExtractorConfig?) {
        this.lastLoadedSummary = summary

        summary.articles.forEach { it.articleSummaryExtractorConfig = extractorConfig }
    }


    fun getAndShowArticlesAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            doActionOnEachItem(items) { item, callback ->
                getAndShowArticle(item, callback)
            }

            done?.invoke()
        }
    }

    open fun getAndShowArticle(item: ArticleSummaryItem, callback: ((Boolean) -> Unit)? = null) {
        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(item) { result ->
            result.result?.let { showArticle(it) }

            callback?.invoke(true)
        }
    }

    protected open fun showArticle(extractionResult: ItemExtractionResult) {
        router.showEditItemView(extractionResult)
    }


    fun getAndSaveArticlesAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            doActionOnEachItem(items) { item, callback ->
                getAndSaveArticle(item, callback)
            }

            done?.invoke()
        }
    }

    fun getAndSaveArticle(item: ArticleSummaryItem, callback: ((Boolean) -> Unit)? = null) {
        getArticle(item) {
            it.result?.let { saveArticle(item, it) }

            callback?.invoke(it.successful)
        }
    }

    private fun saveArticle(item: ArticleSummaryItem, extractionResult: ItemExtractionResult) {
        itemPersister.saveItemAsync(extractionResult) { successful ->
            if(successful) {
                dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved", item.title))
            }
        }
    }


    fun getAndSaveArticlesForLaterReadingAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            doActionOnEachItem(items) { item, callback ->
                getAndSaveArticleForLaterReading(item, callback)
            }

            done?.invoke()
        }
    }

    fun getAndSaveArticleForLaterReading(item: ArticleSummaryItem, callback: ((Boolean) -> Unit)? = null) {
        getArticle(item) {
            it.result?.let { saveArticleForLaterReading(item, it) }

            callback?.invoke(it.successful)
        }
    }

    private fun saveArticleForLaterReading(item: ArticleSummaryItem, result: ItemExtractionResult) {
        readLaterArticleService.persist(ReadLaterArticle(result, "", item.title, item.url, item.previewImageUrl)) // articlePreview gets set in ReadLaterArticleService

        dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved.for.later.reading", item.title))
    }


    protected fun getArticle(item: ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        articleExtractorManager.extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(item) { asyncResult ->
            asyncResult.error?.let {
                showError("alert.message.could.not.load.article", it)
            }

            callback(asyncResult)
        }
    }

    private fun doActionOnEachItem(items: Collection<ArticleSummaryItem>, action: (ArticleSummaryItem, (Boolean) -> Unit) -> Unit) {
        val countDownLatch = CountDownLatch(items.size)

        ArrayList(items).forEach { item ->
            action(item) { successful ->
                if(successful) {
                    (items as? MutableCollection)?.remove(item)
                }

                countDownLatch.countDown()
            }
        }

        try { countDownLatch.await() } catch(ignored: Exception) { }
    }


    fun copySourceUrlToClipboard(item: ArticleSummaryItem) {
        clipboardService.copyUrlToClipboard(item.url)
    }


    private fun showError(errorMessageResourceKey: String, error: Exception, vararg errorMessageArguments: String) {
        dialogService.showErrorMessage(localization.getLocalizedString(errorMessageResourceKey, *errorMessageArguments), exception = error)
    }

}