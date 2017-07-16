package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.EntriesWithoutTagsCalculatedTag
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.EntriesSearch
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class EntriesListPresenter(private val entriesListView: IEntriesListView, router: IRouter, private var searchEngine: ISearchEngine,
                           deleteEntityService: DeleteEntityService, clipboardService: IClipboardService)
    : EntriesListPresenterBase(deleteEntityService, clipboardService, router), IMainViewSectionPresenter {

    private var unfilteredEntries: List<Entry> = listOf()

    private var selectedTag: Tag? = null

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

    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener {
            searchEntries(Search.EmptySearchTerm) {
                unfilteredEntries = it
            }
        }
    }

    fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        selectedTag = tag
        unfilteredEntries = entries

        searchEntries(lastSearchTermProperty) // apply lastSearchTerm on unfilteredEntries
    }


    fun searchEntries(searchTerm: String, searchInContent: Boolean = true, searchInAbstract: Boolean = true, searchInReference: Boolean = true, searchCompleted: ((List<Entry>) -> Unit)? = null) {
       lastSearchTermProperty = searchTerm

        var filterOnlyEntriesWithoutTags = false
        val entriesMustHaveTheseTags = mutableListOf<Tag>()

        selectedTag?.let {
            if(it is CalculatedTag == false) {
                entriesMustHaveTheseTags.add(it)
            }
            filterOnlyEntriesWithoutTags = it is EntriesWithoutTagsCalculatedTag
        }

        searchEngine.searchEntries(EntriesSearch(searchTerm, searchInContent, searchInAbstract, searchInReference, filterOnlyEntriesWithoutTags, entriesMustHaveTheseTags) { result ->
            entriesListView.showEntries(result)

            if(searchCompleted != null) {
                searchCompleted(result)
            }
        })
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    inner class EventBusListener {

        @Handler()
        fun entriesChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            searchEntries(lastSearchTermProperty)
        }

    }

}