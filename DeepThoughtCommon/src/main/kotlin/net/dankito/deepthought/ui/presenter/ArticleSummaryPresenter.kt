package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.extensions.extractor
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.extensions.SeriesAndPublishingDateAndEntryPreviewSeparator
import net.dankito.deepthought.model.extensions.getSeriesAndPublishingDatePreview
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.data.EntryPersister
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


open class ArticleSummaryPresenter(protected val entryPersister: EntryPersister, protected val readLaterArticleService: ReadLaterArticleService, protected val articleExtractorManager: ArticleExtractorManager,
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
            this.lastLoadedSummary = summary
            setArticleSummaryExtractorConfigOnItems(summary, extractorConfig)
        }

        result.error?.let { error -> showError("alert.message.could.not.load.article.summary", error) }

        callback(result)
    }

    fun setArticleSummaryExtractorConfigOnItems(articleSummary: ArticleSummary, extractorConfig: ArticleSummaryExtractorConfig?) {
        articleSummary.articles.forEach { it.articleSummaryExtractorConfig = extractorConfig }
    }


    fun getAndShowArticlesAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            items.forEach { getAndShowArticle(it) }

            done?.invoke()
        }
    }

    open fun getAndShowArticle(item: ArticleSummaryItem) {
        val extractionResult = EntryExtractionResult(Entry("", item.summary), Reference(item.url, item.title, item.publishedDate, item.previewImageUrl))

        articleExtractorManager.addDefaultData(item, extractionResult) {
            showArticle(extractionResult)
        }
    }

    protected open fun showArticle(extractionResult: EntryExtractionResult) {
        router.showEditEntryView(extractionResult)
    }


    fun getAndSaveArticlesAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            items.forEach { getAndSaveArticle(it) }

            done?.invoke()
        }
    }

    fun getAndSaveArticle(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { saveArticle(item, it) }
        }
    }

    private fun saveArticle(item: ArticleSummaryItem, extractionResult: EntryExtractionResult) {
        entryPersister.saveEntryAsync(extractionResult) { successful ->
            if(successful) {
                dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved", item.title))
            }
        }
    }


    fun getAndSaveArticlesForLaterReadingAsync(items: Collection<ArticleSummaryItem>, done: (() -> Unit)?) {
        threadPool.runAsync {
            items.forEach { getAndSaveArticleForLaterReading(it) }

            done?.invoke()
        }
    }

    fun getAndSaveArticleForLaterReading(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { saveArticleForLaterReading(item, it) }
        }
    }

    private fun saveArticleForLaterReading(item: ArticleSummaryItem, result: EntryExtractionResult) {
        var entryPreview = item.summary
        val seriesAndPublishingDate = result.reference.getSeriesAndPublishingDatePreview(result.series)
        if(seriesAndPublishingDate.isNullOrBlank() == false) {
            if(entryPreview.isNullOrBlank()) {
                entryPreview = seriesAndPublishingDate
            }
            else {
                entryPreview = seriesAndPublishingDate + SeriesAndPublishingDateAndEntryPreviewSeparator + entryPreview
            }
        }

        readLaterArticleService.persist(ReadLaterArticle(result, entryPreview, item.title, item.url, item.previewImageUrl))

        dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved.for.later.reading", item.title))
    }


    protected fun getArticle(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        articleExtractorManager.extractArticleAndAddDefaultDataAsync(item) { asyncResult ->
            asyncResult.error?.let {
                showError("alert.message.could.not.load.article", it)
            }

            callback(asyncResult)
        }
    }


    fun copyReferenceUrlToClipboard(item: ArticleSummaryItem) {
        clipboardService.copyUrlToClipboard(item.url)
    }


    private fun showError(errorMessageResourceKey: String, error: Exception, vararg errorMessageArguments: String) {
        dialogService.showErrorMessage(localization.getLocalizedString(errorMessageResourceKey, *errorMessageArguments), exception = error)
    }

}