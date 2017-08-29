package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagSearchResultState
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.FilteredTagsSearch
import net.dankito.service.search.specific.FilteredTagsSearchResult
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


abstract class TagsListPresenterBase(protected val tagsListView: ITagsListView, protected val searchEngine: ISearchEngine, protected val tagService: TagService,
                                     protected val deleteEntityService: DeleteEntityService, protected val searchResultsUtil: TagsSearchResultsUtil, protected val dialogService: IDialogService) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm

    protected var lastTagsSearchResults: TagsSearchResults? = null

    protected var lastFilteredTagsSearchResults: FilteredTagsSearchResult? = null

    protected val tagsFilter = ArrayList<Tag>()


    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var localization: Localization


    private val eventBusListener = EventBusListener()



    protected fun initialized() {
        eventBus.register(eventBusListener)
    }

    fun destroy() {
        eventBus.unregister(eventBusListener)
    }


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
        searchEngine.searchTags(TagsSearch(searchTerm) { result ->
            this.lastTagsSearchResults = result
            this.lastFilteredTagsSearchResults = null

            val tags = getTagsFromSearchTagsWithoutFilterResult(result)

            tagsListView.showTags(tags)
        })
    }

    protected open fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        // may be overwritten in sub class
        return result.getRelevantMatchesSorted()
    }

    private fun searchFilteredTags(searchTerm: String, tagsFilter: List<Tag>) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchFilteredTags(FilteredTagsSearch(tagsFilter, searchTerm) { result ->
            this.lastTagsSearchResults = null
            this.lastFilteredTagsSearchResults = result

            tagsListView.showTags(result.tagsOnEntriesContainingFilteredTags)
        })
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
                tagService.update(tag)
            }
        }
    }

    fun deleteTag(tag: Tag) {
        deleteEntityService.deleteTag(tag)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            when(entitiesOfTypeChanged.entityType) {
                Tag::class.java -> searchTags()
                Entry::class.java -> tagsListView.updateDisplayedTags() // count entries on tag(s) may have changed
            }
        }

    }

}