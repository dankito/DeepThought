package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class ReferencesListPresenter(private var view: IReferencesListView, private var router: IRouter, private var searchEngine: ISearchEngine,
                              private val referenceService: ReferenceService, private val clipboardService: IClipboardService)
    : IMainViewSectionPresenter {

    private var lastSearchTermProperty = Search.EmptySearchTerm


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)
        }
    }


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener {
            searchReferences(Search.EmptySearchTerm)
        }
    }

    private fun retrieveAndShowReferences() {
        searchReferences(lastSearchTermProperty)
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


    fun showEntriesForReference(reference: Reference) {
        router.showEntriesForReference(reference)
    }


    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            if(entitiesOfTypeChanged.entityType == Reference::class.java) {
                retrieveAndShowReferences()
            }
        }

    }

}