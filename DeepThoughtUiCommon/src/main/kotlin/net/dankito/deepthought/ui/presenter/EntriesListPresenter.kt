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
import net.dankito.service.search.specific.EntriesSearch
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

    private var lastEntriesSearch: EntriesSearch? = null


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
        lastEntriesSearch?.interrupt()
        lastSearchTermProperty = searchTerm

        val entriesSearch = createEntriesSearch(searchTerm, searchInContent, searchInAbstract, searchInTags, searchInReference, searchInFiles) { result ->
            entriesListView.showEntities(result)

            searchCompleted?.invoke(result)
        }

        this.lastEntriesSearch = entriesSearch
        searchEngine.searchEntries(entriesSearch)
    }

    private fun createEntriesSearch(searchTerm: String, searchInContent: Boolean, searchInAbstract: Boolean, searchInTags: Boolean, searchInReference: Boolean,
                                    searchInFiles: Boolean, searchCompleted: (List<Item>) -> Unit): EntriesSearch {
        var filterOnlyEntriesWithoutTags = false
        val entriesMustHaveTheseTags = ArrayList(tagsFilter)

        selectedTag?.let {
            if(it is CalculatedTag == false) {
                entriesMustHaveTheseTags.add(it)
            }
            filterOnlyEntriesWithoutTags = it is EntriesWithoutTagsCalculatedTag
        }

        return EntriesSearch(searchTerm, searchInContent, searchInAbstract, searchInTags, searchInReference, searchInFiles, filterOnlyEntriesWithoutTags,
                entriesMustHaveTheseTags, entriesMustHaveThisSource = selectedSource, completedListener = searchCompleted)
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