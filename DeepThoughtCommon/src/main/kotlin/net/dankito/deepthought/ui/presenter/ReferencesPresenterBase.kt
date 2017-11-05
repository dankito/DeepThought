package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService


abstract class ReferencesPresenterBase(private var searchEngine: ISearchEngine, protected var router: IRouter, private val clipboardService: IClipboardService,
                                       private val deleteEntityService: DeleteEntityService) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm


    fun searchReferences(searchTerm: String, searchCompleted: ((List<Source>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchReferences(ReferenceSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        })
    }

    protected open fun retrievedSearchResults(result: List<Source>) {

    }


    fun editReference(source: Source) {
        router.showEditReferenceView(source)
    }

    fun copyReferenceUrlToClipboard(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun deleteReference(source: Source) {
        deleteEntityService.deleteReference(source)
    }

}