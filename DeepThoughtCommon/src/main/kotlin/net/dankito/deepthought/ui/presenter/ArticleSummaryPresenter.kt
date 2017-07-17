package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.extensions.extractor
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearch
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


open class ArticleSummaryPresenter(protected val entryPersister: EntryPersister, protected val readLaterArticleService: ReadLaterArticleService,
                                   protected val tagService: TagService, protected val searchEngine: ISearchEngine, protected val router: IRouter,
                                   protected val dialogService: IDialogService) {


    @Inject
    protected lateinit var articleExtractors: ArticleExtractors

    @Inject
    protected lateinit var localization: Localization


    init {
        CommonComponent.component.inject(this)
    }


    fun extractArticlesSummary(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.extractSummaryAsync {
            retrievedArticleSummary(it, extractorConfig, callback)
        }
    }

    fun loadMoreItems(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.loadMoreItemsAsync {
            retrievedArticleSummary(it, extractorConfig, callback)
        }
    }

    private fun retrievedArticleSummary(result: AsyncResult<out ArticleSummary>, extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        result.result?.let { summary -> setArticleSummaryExtractorConfigOnItems(summary, extractorConfig) }

        result.error?.let { error -> showError("alert.message.could.not.load.article.summary", error, error.localizedMessage) }

        callback(result)
    }

    fun setArticleSummaryExtractorConfigOnItems(articleSummary: ArticleSummary, extractorConfig: ArticleSummaryExtractorConfig?) {
        articleSummary.articles.forEach { it.articleSummaryExtractorConfig = extractorConfig }
    }


    fun getAndShowArticle(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { showArticle(it) }
        }
    }

    protected open fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
    }

    fun getAndSaveArticle(item: ArticleSummaryItem) {
        getArticle(item) {
            it.result?.let { saveArticle(item, it) }
        }
    }

    private fun saveArticle(item: ArticleSummaryItem, extractionResult: EntryExtractionResult) {
        if(entryPersister.saveEntry(extractionResult)) {
            dialogService.showLittleInfoMessage(localization.getLocalizedString("article.summary.extractor.article.saved", item.title))
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
                    showError("alert.message.could.not.load.article", it, item.url, it.localizedMessage)
                }
            }
        }
    }

    private fun retrievedArticle(extractor: IArticleExtractor, item: ArticleSummaryItem, asyncResult: AsyncResult<EntryExtractionResult>,
                                 extractionResult: EntryExtractionResult, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        getDefaultTagsForExtractor(extractor, item) { tags ->
            extractionResult.tags.addAll(tags)
            callback(asyncResult)
        }
    }

    private fun getDefaultTagsForExtractor(extractor: IArticleExtractor, item: ArticleSummaryItem, callback: (List<Tag>) -> Unit) {
        extractor.getName()?.let { extractorName ->
            getTagForExtractorName(extractorName, callback)
        }

        if(extractor.getName() == null && item.articleSummaryExtractorConfig?.name != null) {
            item.articleSummaryExtractorConfig?.name?.let { getTagForExtractorName(it, callback) }
        }

        if(extractor.getName() == null && item.articleSummaryExtractorConfig?.name == null) {
            callback(listOf())
        }
    }

    private fun getTagForExtractorName(extractorName: String, callback: (List<Tag>) -> Unit) {
        searchEngine.searchTags(TagsSearch(extractorName) { tagsSearchResults ->
            if(tagsSearchResults.exactMatchesOfLastResult.isNotEmpty()) {
                callback(tagsSearchResults.exactMatchesOfLastResult)
                return@TagsSearch
            }

            val extractorTag = Tag(extractorName) // no tag with name 'extractorName' found -> create new one

            tagService.persist(extractorTag)

            callback(listOf(extractorTag))
        })
    }


    private fun showError(errorMessageResourceKey: String, error: Exception, vararg errorMessageArguments: String) {
        dialogService.showErrorMessage(localization.getLocalizedString(errorMessageResourceKey, *errorMessageArguments), exception = error)
    }

}