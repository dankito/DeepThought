package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.AllEntriesCalculatedTag
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.EntriesWithoutTagsCalculatedTag
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.jpa.relationship.collections.LazyLoadingEntitiesCollection
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.results.FilteredTagsLazyLoadingLuceneSearchResultsList
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.service.search.util.CombinedLazyLoadingList
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject
import kotlin.concurrent.thread


class TagsListPresenter(tagsListView: ITagsListView, private val dataManager: DataManager, searchEngine: ISearchEngine, searchResultsUtil: TagsSearchResultsUtil, tagService: TagService,
                        deleteEntityService: DeleteEntityService, dialogService: IDialogService, private val router: IRouter)
    : TagsListPresenterBase(tagsListView, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService), IMainViewSectionPresenter {


    private var hasInitializedCalculatedTags = false

    protected val calculatedTags = ArrayList<CalculatedTag>()

    var tagFilterListener: ((List<Tag>) -> Unit)? = null


    @Inject
    protected lateinit var entityChangedNotifier: EntityChangedNotifier


    init {
        thread {
            CommonComponent.component.inject(this)

            initialized()
        }
    }

    override fun cleanUp() {
        destroy()
    }


    override fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        if(result.hasEmptySearchTerm) {
            if(hasInitializedCalculatedTags == false) {
                initCalculatedTags()
            }

            return CombinedLazyLoadingList<Tag>(calculatedTags, result.getRelevantMatchesSorted())
        }

        return super.getTagsFromSearchTagsWithoutFilterResult(result)
    }

    private fun initCalculatedTags() {
        calculatedTags.add(AllEntriesCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))
        calculatedTags.add(EntriesWithoutTagsCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))

        hasInitializedCalculatedTags = true
    }


    fun isTagFiltered(tag: Tag): Boolean {
        return tagsFilter.contains(tag)
    }

    fun clearTagFilter() {
        tagsFilter.clear()

        searchTags()
    }

    fun toggleFilterTag(tag: Tag) {
        doToggleFilterTag(tag)

        searchTags()
    }

    fun toggleFilterTags(tags: List<Tag>) {
        if(tags.isNotEmpty()) {
            tags.forEach { tag ->
                doToggleFilterTag(tag)
            }
        }
        else {
            if(isTagFilterApplied()) { // after tags filter has applied no tags are shown anymore (there are no entries having these tags) -> clear tag filter
                clearTagFilter()
            }
        }

        searchTags()
    }

    private fun doToggleFilterTag(tag: Tag) {
        if(isTagFiltered(tag)) {
            tagsFilter.remove(tag)
        }
        else {
            tagsFilter.add(tag)
        }

        tagFilterListener?.invoke(tagsFilter)
    }

    fun getCountEntriesForFilteredTag(tag: Tag): Int {
        lastFilteredTagsSearchResults?.let {
            val allFilteredEntryIds = (it.entriesHavingFilteredTags as FilteredTagsLazyLoadingLuceneSearchResultsList).entityIds // TODO: this is bad code, uses knowledge of  implementation details

            (tag.entries as? LazyLoadingEntitiesCollection)?.let { // TODO: here as well
                val filteredEntriesOnTag = ArrayList(it.targetEntitiesIds)
                filteredEntriesOnTag.retainAll(allFilteredEntryIds)
                return filteredEntriesOnTag.size
            }
        }

        return tag.countEntries
    }


    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    fun showEntriesForTag(tag: Tag) {
        router.showEntriesForTag(tag, tagsFilter)
    }

}