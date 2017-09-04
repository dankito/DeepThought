package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class ReferencesListPresenter(private var view: IReferencesListView, private var router: IRouter, searchEngine: ISearchEngine,
                              clipboardService: IClipboardService, deleteEntityService: DeleteEntityService)
    : ReferencesPresenterBase(searchEngine, clipboardService, deleteEntityService), IMainViewSectionPresenter {


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)
        }
    }


    private fun retrieveAndShowReferences() {
        searchReferences(lastSearchTermProperty)
    }

    override fun retrievedSearchResults(result: List<Reference>) {
        super.retrievedSearchResults(result)

        view.showEntities(result)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    fun editReference(reference: Reference) {
        router.showEditReferenceView(reference)
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