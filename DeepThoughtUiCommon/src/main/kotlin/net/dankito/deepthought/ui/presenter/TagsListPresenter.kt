package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.AllCalculatedTags
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.jpa.relationship.collections.LazyLoadingEntitiesCollection
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.results.FilteredTagsLazyLoadingLuceneSearchResultsList
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.service.search.util.CombinedLazyLoadingList
import net.dankito.utils.ui.dialogs.IDialogService
import kotlin.concurrent.thread


class TagsListPresenter(tagsListView: ITagsListView, private val allCalculatedTags: AllCalculatedTags, searchEngine: ISearchEngine, searchResultsUtil: TagsSearchResultsUtil,
                        tagService: TagService, deleteEntityService: DeleteEntityService, dialogService: IDialogService, private val router: IRouter)
    : TagsListPresenterBase(tagsListView, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService) {


    var tagFilterListener: ((List<Tag>) -> Unit)? = null


    init {
        thread {
            CommonComponent.component.inject(this)
        }
    }


    override fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        if(result.hasEmptySearchTerm) {

            return CombinedLazyLoadingList<Tag>(allCalculatedTags.getCalculatedTags(), super.getTagsFromSearchTagsWithoutFilterResult(result))
        }

        return super.getTagsFromSearchTagsWithoutFilterResult(result)
    }


    fun isTagFiltered(tag: Tag): Boolean {
        return tagsFilter.contains(tag)
    }

    fun clearTagFilter() {
        tagsFilter.clear()

        tagFilterListener?.invoke(tagsFilter)

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
            if(isTagFilterApplied()) { // after tags filter has applied no tags are shown anymore (there are no items having these tags) -> clear tag filter
                clearTagFilter()
            }
        }

        searchTags()
    }

    fun toggleFilterTagsOfLastSearchResult() {
        lastTagsSearchResults?.let { searchResult ->
            toggleFilterTags(searchResult.getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible())
        }

        lastFilteredTagsSearchResults?.let { searchResult ->
            toggleFilterTags(searchResult.tagsOnItemsContainingFilteredTags)
        }
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

    fun getCountItemsForFilteredTag(tag: Tag): Int {
        lastFilteredTagsSearchResults?.let {
            // TODO: this is bad code, uses knowledge of  implementation details
            (it.itemsHavingFilteredTags as? FilteredTagsLazyLoadingLuceneSearchResultsList)?.entityIds?.let { allFilteredItemIds ->
                (tag.items as? LazyLoadingEntitiesCollection)?.let { // TODO: here as well
                    val filteredItemsOnTag = ArrayList(it.targetEntitiesIds)
                    filteredItemsOnTag.retainAll(allFilteredItemIds)
                    return filteredItemsOnTag.size
                }
            }
        }

        return 0 // there are not items having this combination of tags
    }


    fun showItemsForTag(tag: Tag) {
        router.showItemsForTag(tag, tagsFilter)
    }


    override fun viewBecomesVisible() {
        super.viewBecomesVisible()

        allCalculatedTags.getCalculatedTags().forEach {
            it.tagBecomesVisible()
        }
    }

    override fun viewGetsHidden() {
        allCalculatedTags.getCalculatedTags().forEach {
            it.tagGetsHidden()
        }

        super.viewGetsHidden()
    }

}