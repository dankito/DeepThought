package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.extensions.extractor
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.SeriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


open class ArticleSummaryPresenter(protected val entryPersister: EntryPersister, protected val readLaterArticleService: ReadLaterArticleService,
                                   protected val tagService: TagService, protected val seriesService: SeriesService, protected val searchEngine: ISearchEngine, protected val router: IRouter,
                                   protected val dialogService: IDialogService) {


    @Inject
    protected lateinit var articleExtractors: ArticleExtractors

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

    fun getAndShowArticle(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { showArticle(it) }
        }
    }

    protected open fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
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
        readLaterArticleService.persist(ReadLaterArticle(result))

        dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved.for.later.reading", item.title))
    }


    private fun getArticle(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { retrievedArticle(extractor, item, asyncResult, it, callback) }
                asyncResult.error?.let {
                    callback(asyncResult)
                    showError("alert.message.could.not.load.article", it)
                }
            }
        }
    }

    private fun retrievedArticle(extractor: IArticleExtractor, item: ArticleSummaryItem, asyncResult: AsyncResult<EntryExtractionResult>,
                                 extractionResult: EntryExtractionResult, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        getDefaultTagsForExtractor(extractor, item) { tag ->
            tag?.let { extractionResult.tags.add(tag) }

            setSeries(extractionResult, extractor, item, asyncResult, callback)
        }
    }

    private fun getDefaultTagsForExtractor(extractor: IArticleExtractor, item: ArticleSummaryItem, callback: (Tag?) -> Unit) {
        extractor.getName()?.let { extractorName ->
            getTagForExtractorNameSynchronized(extractorName, callback)
        }

        if(extractor.getName() == null && item.articleSummaryExtractorConfig?.name != null) {
            item.articleSummaryExtractorConfig?.name?.let { getTagForExtractorNameSynchronized(it, callback) }
        }

        if(extractor.getName() == null && item.articleSummaryExtractorConfig?.name == null) {
            callback(null)
        }
    }

    /**
     * To avoid that when multiple entries get fetched in parallel that multiple tags get created for one extractor this method synchronizes access to getTagForExtractorName()
     */
    private fun getTagForExtractorNameSynchronized(extractorName: String, callback: (Tag) -> Unit) {
        synchronized(this) {
            val countDownLatch = CountDownLatch(1)

            getTagForExtractorName(extractorName) {
                countDownLatch.countDown()

                callback(it)
            }

            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }

    private fun getTagForExtractorName(extractorName: String, callback: (Tag) -> Unit) {
        searchEngine.searchTags(TagsSearch(extractorName) { tagsSearchResults ->
            if(tagsSearchResults.exactMatchesOfLastResult.isNotEmpty()) {
                callback(tagsSearchResults.exactMatchesOfLastResult.first())
                return@TagsSearch
            }

            val extractorTag = Tag(extractorName) // no tag with name 'extractorName' found -> create new one

            tagService.persist(extractorTag)

            callback(extractorTag)
        })
    }


    private fun setSeries(extractionResult: EntryExtractionResult, extractor: IArticleExtractor, item: ArticleSummaryItem, asyncResult: AsyncResult<EntryExtractionResult>, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        extractionResult.reference?.let { reference ->
            if(reference.series == null || reference.series?.isPersisted() == false) { // series not set to a persisted Series -> try to find an existing one or create and persist a new one
                val seriesTitle = reference.series?.title ?: extractor.getName() ?: item.articleSummaryExtractorConfig?.name

                if(seriesTitle != null) {
                    getSeriesForTitleSynchronized(seriesTitle) {
                        reference.series = it
                        callback(asyncResult)
                    }

                    return // avoid that callback() at end of this method gets called
                }
            }
        }

        callback(asyncResult)
    }

    /**
     * To avoid that when multiple entries get fetched in parallel that multiple tags get created for one extractor this method synchronizes access to getTagForExtractorName()
     */
    private fun getSeriesForTitleSynchronized(seriesTitle: String, callback: (Series) -> Unit) {
        synchronized(this) {
            val countDownLatch = CountDownLatch(1)

            getSeriesForTitle(seriesTitle) {
                countDownLatch.countDown()

                callback(it)
            }

            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }

    private fun getSeriesForTitle(seriesTitle: String, callback: (Series) -> Unit) {
        searchEngine.searchSeries(SeriesSearch(seriesTitle) { searchResults ->
            if(searchResults.isNotEmpty()) {
                callback(searchResults.first())
                return@SeriesSearch
            }

            val series = Series(seriesTitle) // no Series with name 'seriesTitle' found -> create new one

            seriesService.persist(series)

            callback(series)
        })
    }


    private fun showError(errorMessageResourceKey: String, error: Exception, vararg errorMessageArguments: String) {
        dialogService.showErrorMessage(localization.getLocalizedString(errorMessageResourceKey, *errorMessageArguments), exception = error)
    }

}