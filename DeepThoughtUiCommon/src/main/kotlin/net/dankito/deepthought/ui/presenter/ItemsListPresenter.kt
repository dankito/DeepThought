package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.IItemsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.service.search.util.SortOption
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class ItemsListPresenter(private val itemsListView: IItemsListView, router: IRouter, private val searchEngine: ISearchEngine,
                         deleteEntityService: DeleteEntityService, dialogService: IDialogService, clipboardService: IClipboardService, threadPool: IThreadPool)
    : ItemsListPresenterBase(deleteEntityService, dialogService, clipboardService, router, threadPool), IMainViewSectionPresenter {

    private var tagsFilter: List<Tag> = listOf()

    private var selectedTag: Tag? = null

    private var selectedSource: Source? = null

    private var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastItemsSearch: ItemsSearch? = null

    private val itemsSortOptions: MutableList<SortOption> = mutableListOf(SortOption(FieldName.ItemCreated, false)) // = default sort option: sort by created descending


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)
        }
    }


    fun createItem() {
        router.showCreateItemView()
    }

    fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>) {
        selectedSource = null

        selectedTag = tag
        this.tagsFilter = tagsFilter

        searchItems() // apply lastSearchTerm on unfiltered items
    }

    fun showItemsForSource(source: Source) {
        selectedTag = null
        tagsFilter = emptyList()

        this.selectedSource = source

        searchItems()
    }


    private fun searchItems() {
        searchItems(getLastSearchTerm())
    }

    fun searchItems(searchTerm: String, searchInContent: Boolean = true, searchInSummary: Boolean = true, searchInSource: Boolean = true, searchInTags: Boolean = true,
                    searchInFiles: Boolean = true, sortOrder: List<SortOption> = itemsSortOptions, searchCompleted: ((List<Item>) -> Unit)? = null) {
        lastItemsSearch?.interrupt()
        lastSearchTermProperty = searchTerm

        val itemsSearch = createItemsSearch(searchTerm, searchInContent, searchInSummary, searchInTags, searchInSource, searchInFiles, sortOrder) { result ->
            itemsListView.showEntities(result)

            searchCompleted?.invoke(result)
        }

        this.lastItemsSearch = itemsSearch
        searchEngine.searchItems(itemsSearch)
    }

    private fun createItemsSearch(searchTerm: String, searchInContent: Boolean, searchInSummary: Boolean, searchInTags: Boolean, searchInSource: Boolean,
                                  searchInFiles: Boolean, sortOrder: List<SortOption>, searchCompleted: (List<Item>) -> Unit): ItemsSearch {
        var searchOnlyItemsWithoutTags = false
        val itemsMustHaveTheseTags = ArrayList(tagsFilter)

        selectedTag?.let {
            if(it is CalculatedTag == false) {
                itemsMustHaveTheseTags.add(it)
            }
            searchOnlyItemsWithoutTags = it is ItemsWithoutTagsCalculatedTag
        }

        return ItemsSearch(searchTerm, searchInContent, searchInSummary, searchInTags, searchInSource, searchInFiles, searchOnlyItemsWithoutTags,
                itemsMustHaveTheseTags, itemsMustHaveThisSource = selectedSource, sortOptions = sortOrder, completedListener = searchCompleted)
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    fun isSortOptionSet(propertyName: String, ascending: Boolean): Boolean {
        return itemsSortOptions.firstOrNull { it.property == propertyName && it.ascending == ascending } != null
    }

    fun setSortOptionsAsync(sortOptions: List<SortOption>) {
        threadPool.runAsync {
            setSortOptions(sortOptions)
        }
    }

    fun setSortOptions(sortOptions: List<SortOption>) {
        this.itemsSortOptions.clear()

        this.itemsSortOptions.addAll(sortOptions)

        searchItems()
    }


    override fun viewBecomesVisible() {
        eventBus.register(eventBusListener)
    }

    override fun viewGetsHidden() {
        eventBus.unregister(eventBusListener)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entityChanged: EntitiesOfTypeChanged) {
            if(entityChanged.entityType == Item::class.java) {
                searchItems(lastSearchTermProperty)
            }
        }

    }

}