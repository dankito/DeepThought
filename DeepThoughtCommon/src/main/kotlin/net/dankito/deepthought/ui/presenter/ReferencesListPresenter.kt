package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.ReferenceService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService


class ReferencesListPresenter(private var view: IReferencesListView, private var router: IRouter, private var searchEngine: ISearchEngine,
                              private val referenceService: ReferenceService, private val clipboardService: IClipboardService)
    : IMainViewSectionPresenter {

    private var lastSearchTermProperty = Search.EmptySearchTerm


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener {
            searchReferences(Search.EmptySearchTerm)
        }
    }

    fun searchReferences(searchTerm: String, searchCompleted: ((List<Reference>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchReferences(ReferenceSearch(searchTerm) { result ->
            view.showReferences(result)

            searchCompleted?.invoke(result)
        })
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }

    override fun cleanUp() {
    }

}