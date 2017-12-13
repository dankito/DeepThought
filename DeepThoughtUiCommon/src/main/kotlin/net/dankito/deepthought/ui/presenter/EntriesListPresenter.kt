package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.EntriesWithoutTagsCalculatedTag
import net.dankito.deepthought.model.Item
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


class EntriesListPresenter(private val entriesListView: IEntriesListView, private val router: IRouter, private val searchEngine: ISearchEngine,
                           deleteEntityService: DeleteEntityService, clipboardService: IClipboardService)
    : EntriesListPresenterBase(deleteEntityService, clipboardService, router), IMainViewSectionPresenter {

    private var tagsFilter: List<Tag> = listOf()

    private var selectedTag: Tag? = null

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
        selectedTag = tag
        this.tagsFilter = tagsFilter

        searchEntries(lastSearchTermProperty) // apply lastSearchTerm on unfilteredEntries
    }


    fun searchEntries(searchTerm: String, searchInContent: Boolean = true, searchInAbstract: Boolean = true, searchInReference: Boolean = true, searchInTags: Boolean = true,
                      searchCompleted: ((List<Item>) -> Unit)? = null) {
        lastEntriesSearch?.interrupt()
        lastSearchTermProperty = searchTerm

        var filterOnlyEntriesWithoutTags = false
        val entriesMustHaveTheseTags = ArrayList(tagsFilter)

        selectedTag?.let {
            if(it is CalculatedTag == false) {
                entriesMustHaveTheseTags.add(it)
            }
            filterOnlyEntriesWithoutTags = it is EntriesWithoutTagsCalculatedTag
        }

        lastEntriesSearch = EntriesSearch(searchTerm, searchInContent, searchInAbstract, searchInReference, searchInTags, filterOnlyEntriesWithoutTags, entriesMustHaveTheseTags) { result ->
            entriesListView.showEntities(result)

            searchCompleted?.invoke(result)
        }

        searchEngine.searchEntries(lastEntriesSearch!!)
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