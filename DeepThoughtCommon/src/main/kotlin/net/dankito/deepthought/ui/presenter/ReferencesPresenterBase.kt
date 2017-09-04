package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService


abstract class ReferencesPresenterBase(private var searchEngine: ISearchEngine, private val clipboardService: IClipboardService,
                                       private val deleteEntityService: DeleteEntityService) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm


    fun searchReferences(searchTerm: String, searchCompleted: ((List<Reference>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchReferences(ReferenceSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        })
    }

    protected open fun retrievedSearchResults(result: List<Reference>) {

    }


    fun copyReferenceUrlToClipboard(reference: Reference) {
        clipboardService.copyReferenceUrlToClipboard(reference)
    }

    fun deleteReference(reference: Reference) {
        deleteEntityService.deleteReference(reference)
    }

}