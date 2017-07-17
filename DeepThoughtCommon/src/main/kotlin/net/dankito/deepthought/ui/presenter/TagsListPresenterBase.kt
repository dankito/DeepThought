package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagSearchResultState
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.FilteredTagsSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResults
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


abstract class TagsListPresenterBase(protected val tagsListView: ITagsListView, protected val searchEngine: ISearchEngine, protected val searchResultsUtil: TagsSearchResultsUtil) {

    protected var lastSearchTermProperty = Search.EmptySearchTerm

    protected var lastTagsSearchResults: TagsSearchResults? = null

    protected val tagsFilter = ArrayList<Tag>()

    protected val calculatedTags = ArrayList<CalculatedTag>()


    @Inject
    protected lateinit var eventBus: IEventBus


    private val eventBusListener = EventBusListener()



    protected fun initialized() {
        eventBus.register(eventBusListener)
    }

    protected fun destroy() {
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

            tagsListView.showTags(result.tagsOnEntriesContainingFilteredTags)
        })
    }


    fun isTagFilterApplied(): Boolean {
        return tagsFilter.isNotEmpty()
    }


    fun getTagSearchResultState(tag: Tag): TagSearchResultState {
        return searchResultsUtil.getTagSearchResultState(tag, lastTagsSearchResults)
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