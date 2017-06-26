package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearch
import javax.inject.Inject


open class ArticleSummaryPresenter(protected val articleExtractors: ArticleExtractors, protected val entryPersister: EntryPersister,
                                   protected val readLaterArticleService: ReadLaterArticleService, protected val tagService: TagService,
                                   protected val searchEngine: ISearchEngine, protected val router: IRouter) {


    @Inject
    protected lateinit var serializer: ISerializer


    init {
        CommonComponent.component.inject(this)
    }


    fun extractArticlesSummary(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.extractSummaryAsync {
            callback(it)
        }
    }

    fun loadMoreItems(extractorConfig: ArticleSummaryExtractorConfig?, callback: (AsyncResult<out ArticleSummary>) -> Unit) {
        extractorConfig?.extractor?.loadMoreItemsAsync {
            callback(it)
        }
    }


    fun getAndShowArticle(item: ArticleSummaryItem, errorCallback: (Exception) -> Unit) {
        getArticle(item) {
            it.result?.let { showArticle(it) }
            it.error?.let { errorCallback(it) }
        }
    }

    protected open fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
    }

    fun getAndSaveArticle(item: ArticleSummaryItem, errorCallback: (Exception) -> Unit) {
        getArticle(item) {
            it.result?.let { saveArticle(it) }
            it.error?.let { errorCallback(it) }
        }
    }

    private fun saveArticle(it: EntryExtractionResult) = entryPersister.saveEntry(it)

    fun getAndSaveArticleForLaterReading(item: ArticleSummaryItem, errorCallback: (Exception) -> Unit) {
        getArticle(item) {
            it.result?.let { saveArticleForLaterReading(it) }
            it.error?.let { errorCallback(it) }
        }
    }

    private fun saveArticleForLaterReading(result: EntryExtractionResult) {
        val serializedEntryExtractionResult = serializer.serializeObject(result)

        readLaterArticleService.persist(ReadLaterArticle(serializedEntryExtractionResult))
    }


    private fun getArticle(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { retrievedArticle(extractor, item, asyncResult, it, callback) }
                asyncResult.error?.let { callback(asyncResult) }
            }
        }
    }

    private fun retrievedArticle(extractor: IArticleExtractor, item: ArticleSummaryItem, asyncResult: AsyncResult<EntryExtractionResult>,
                                 extractionResult: EntryExtractionResult, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        getDefaultTagsForExtractor(extractor) { tags ->
            extractionResult.tags.addAll(tags)
            callback(asyncResult)
        }
    }

    private fun getDefaultTagsForExtractor(extractor: IArticleExtractor, callback: (List<Tag>) -> Unit) {
        extractor.getName()?.let { extractorName ->
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
    }

}