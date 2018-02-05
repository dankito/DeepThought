package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class EntriesListPresenter(private val entriesListView: IEntriesListView, private val router: IRouter, private val searchEngine: ISearchEngine,
                           deleteEntityService: DeleteEntityService, clipboardService: IClipboardService)
    : EntriesListPresenterBase(deleteEntityService, clipboardService, router), IMainViewSectionPresenter {

    private var tagsFilter: List<Tag> = listOf()

    private var selectedTag: Tag? = null

    private var selectedSource: Source? = null

    private var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastItemsSearch: ItemsSearch? = null


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


    fun createEntry() {
        router.showCreateEntryView()
    }

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        selectedSource = null

        selectedTag = tag
        this.tagsFilter = tagsFilter

        searchEntries() // apply lastSearchTerm on unfilteredEntries
    }

    fun showItemsForSource(source: Source) {
        selectedTag = null
        tagsFilter = emptyList()

        this.selectedSource = source

        searchEntries()
    }


    private fun searchEntries() {
        searchEntries(getLastSearchTerm())
    }

    fun searchEntries(searchTerm: String, searchInContent: Boolean = true, searchInAbstract: Boolean = true, searchInReference: Boolean = true, searchInTags: Boolean = true,
                      searchInFiles: Boolean = true, searchCompleted: ((List<Item>) -> Unit)? = null) {
        lastItemsSearch?.interrupt()
        lastSearchTermProperty = searchTerm

        val entriesSearch = createEntriesSearch(searchTerm, searchInContent, searchInAbstract, searchInTags, searchInReference, searchInFiles) { result ->
            entriesListView.showEntities(result)

            searchCompleted?.invoke(result)
        }

        this.lastItemsSearch = entriesSearch
        searchEngine.searchItems(entriesSearch)
    }

    private fun createEntriesSearch(searchTerm: String, searchInContent: Boolean, searchInSummary: Boolean, searchInTags: Boolean, searchInSource: Boolean,
                                    searchInFiles: Boolean, searchCompleted: (List<Item>) -> Unit): ItemsSearch {
        var searchOnlyItemsWithoutTags = false
        val itemsMustHaveTheseTags = ArrayList(tagsFilter)

        selectedTag?.let {
            if(it is CalculatedTag == false) {
                itemsMustHaveTheseTags.add(it)
            }
            searchOnlyItemsWithoutTags = it is ItemsWithoutTagsCalculatedTag
        }

        return ItemsSearch(searchTerm, searchInContent, searchInSummary, searchInTags, searchInSource, searchInFiles, searchOnlyItemsWithoutTags,
                itemsMustHaveTheseTags, itemsMustHaveThisSource = selectedSource, completedListener = searchCompleted)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entityChanged: EntitiesOfTypeChanged) {
            if(entityChanged.entityType == Item::class.java) {
                searchEntries(lastSearchTermProperty)
            }
        }

    }

}