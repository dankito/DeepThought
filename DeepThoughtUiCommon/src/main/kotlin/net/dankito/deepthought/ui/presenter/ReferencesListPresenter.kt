package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class ReferencesListPresenter(private var view: IReferencesListView, private val searchEngine: ISearchEngine, router: IRouter,
                              clipboardService: IClipboardService, deleteEntityService: DeleteEntityService)
    : ReferencesPresenterBase(router, clipboardService, deleteEntityService), IMainViewSectionPresenter {


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    private var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastSourceSearch: ReferenceSearch? = null


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)
        }
    }


    private fun retrieveAndShowReferences() {
        searchReferences(lastSearchTermProperty)
    }


    fun searchReferences(searchTerm: String, searchCompleted: ((List<Source>) -> Unit)? = null) {
        lastSearchTermProperty = searchTerm

        lastSourceSearch?.interrupt()

        lastSourceSearch = ReferenceSearch(searchTerm) { result ->
            retrievedSearchResults(result)

            searchCompleted?.invoke(result)
        }

        lastSourceSearch?.let { searchEngine.searchReferences(it) }
    }

    private fun retrievedSearchResults(result: List<Source>) {
        view.showEntities(result)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }



    fun showItemsForSource(source: Source) {
        router.showItemsForSource(source)
    }


    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entityChanged: EntitiesOfTypeChanged) {
            if(entityChanged.entityType == Source::class.java) {
                retrieveAndShowReferences()
            }
        }

    }

}