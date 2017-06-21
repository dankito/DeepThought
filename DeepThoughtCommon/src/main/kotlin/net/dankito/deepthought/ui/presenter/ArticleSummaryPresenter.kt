package net.dankito.deepthought.ui.presenter

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearch


open class ArticleSummaryPresenter(protected val articleExtractors: ArticleExtractors, protected val tagService: TagService,
                                   protected val searchEngine: ISearchEngine, protected val router: IRouter) {

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
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let {
                    getDefaultTagsForExtractor(extractor) { tags ->
                        it.tags.addAll(tags)
                        showArticle(it)
                    }
                }
                asyncResult.error?.let { errorCallback(it) }
            }
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

    protected open fun showArticle(extractionResult: EntryExtractionResult) {
        router.showViewEntryView(extractionResult)
    }

}