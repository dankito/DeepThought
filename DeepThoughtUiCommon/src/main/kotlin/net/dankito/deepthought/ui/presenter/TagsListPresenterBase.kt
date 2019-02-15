package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagSearchResultState
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.FilteredTagsSearch
import net.dankito.service.search.specific.FilteredTagsSearchResult
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


abstract class TagsListPresenterBase(protected val tagsListView: ITagsListView, protected val searchEngine: ISearchEngine, protected val tagService: TagService,
                                     protected val deleteEntityService: DeleteEntityService, protected val searchResultsUtil: TagsSearchResultsUtil,
                                     protected val dialogService: IDialogService) : IMainViewSectionPresenter {

    protected var lastSearchTermProperty = Search.EmptySearchTerm

    private var lastTagsSearch: TagsSearch? = null

    var lastTagsSearchResults: TagsSearchResults? = null
        protected set

    private var lastFilteredTagsSearch: FilteredTagsSearch? = null

    protected var lastFilteredTagsSearchResults: FilteredTagsSearchResult? = null

    protected val tagsFilter = ArrayList<Tag>()


    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var localization: Localization

    @Inject
    protected lateinit var threadPool: IThreadPool


    private val eventBusListener = EventBusListener()


    protected fun searchTags() {
        searchTags(lastSearchTermProperty)
    }

    fun searchTags(searchTerm: String) {
        lastSearchTermProperty = searchTerm

        if(isTagFilterApplied()) {
            searchFilteredTags(lastSearchTermProperty, tagsFilter)
        }
        else {
            searchTagsWithoutFilter(lastSearchTermProperty)
        }
    }

    private fun searchTagsWithoutFilter(searchTerm: String) {
        lastTagsSearch?.interrupt()

        lastTagsSearch = TagsSearch(searchTerm) { result ->
            tagsSearchResultsRetrieved(result)
        }

        searchEngine.searchTags(lastTagsSearch!!)
    }

    protected open fun tagsSearchResultsRetrieved(result: TagsSearchResults) {
        this.lastTagsSearchResults = result
        this.lastFilteredTagsSearchResults = null

        val tags = getTagsFromSearchTagsWithoutFilterResult(result)

        tagsListView.showEntities(tags)
    }

    protected open fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        // may be overwritten in sub class
        return result.getRelevantMatchesSorted()
    }

    private fun searchFilteredTags(searchTerm: String, tagsFilter: List<Tag>) {
        lastFilteredTagsSearch?.interrupt()

        lastFilteredTagsSearch = FilteredTagsSearch(tagsFilter, searchTerm) { result ->
            this.lastTagsSearchResults = null
            this.lastFilteredTagsSearchResults = result

            tagsListView.showEntities(result.tagsOnItemsContainingFilteredTags)
        }

        searchEngine.searchFilteredTags(lastFilteredTagsSearch!!)
    }


    fun isTagFilterApplied(): Boolean {
        return tagsFilter.isNotEmpty()
    }


    fun getTagSearchResultState(tag: Tag?): TagSearchResultState {
        return searchResultsUtil.getTagSearchResultState(tag, lastTagsSearchResults)
    }


    fun editTag(tag: Tag) {
        dialogService.askForTextInput(localization.getLocalizedString("alert.message.tag.name"), defaultValue = tag.name) { _, enteredName ->
            enteredName?.let {
                tag.name = enteredName
                tagService.update(tag, true)
            }
        }
    }


    fun confirmDeleteTagsAsync(tags: List<Tag>) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.tags", tags.size)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteTagsAsync(tags)
            }
        }
    }

    fun deleteTagsAsync(tags: List<Tag>) {
        threadPool.runAsync {
            ArrayList(tags).forEach { tag ->
                deleteTag(tag)
            }
        }
    }

    fun confirmDeleteTagAsync(tag: Tag) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.tag", tag.name)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteTagAsync(tag)
            }
        }
    }

    fun deleteTagAsync(tag: Tag) {
        threadPool.runAsync { deleteTag(tag) }
    }

    fun deleteTag(tag: Tag) {
        tagsFilter.remove(tag) // ensure tag gets removed from tagsFilter as otherwise SearchEngine throws a NPE as tag.id then is null

        deleteEntityService.deleteTag(tag)
    }


    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
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
            if(entityChanged.entityType == Tag::class.java) {
                if(entityChanged.changeType == EntityChangeType.Updated) {
                    tagsListView.updateDisplayedTags()
                }
                else {
                    searchTags()
                }
            }
        }

    }

}