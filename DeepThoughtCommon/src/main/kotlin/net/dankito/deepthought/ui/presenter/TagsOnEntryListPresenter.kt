package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.utils.ui.IDialogService


class TagsOnEntryListPresenter(tagsListView: ITagsListView, searchEngine: ISearchEngine, tagService: TagService, deleteEntityService: DeleteEntityService,
                               searchResultsUtil: TagsSearchResultsUtil, dialogService: IDialogService)
    : TagsListPresenterBase(tagsListView, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService) {


    init {
        CommonComponent.component.inject(this)

        initialized()
    }


    fun getButtonStateForSearchResult(): TagsSearcherButtonState {
        return searchResultsUtil.getButtonStateForSearchResult(lastTagsSearchResults)
    }

    fun createNewTags(enteredText: String, tagsOnEntry: MutableCollection<Tag>) {
        lastTagsSearchResults?.let { searchResults ->
            enteredText.split(SearchEngineBase.TagsSearchTermSeparator).map { it.trim() }.forEach { tagName ->
                tagsOnEntry.add(createNewTag(tagName))
            }

            searchTags(searchResults.overAllSearchTerm)
        }
    }

    private fun createNewTag(tagName: String): Tag {
        val newTag = Tag(tagName)

        tagService.persist(newTag)

        return newTag
    }

    fun toggleTagsOnEntry(tagsOnEntry: MutableCollection<Tag>) {
        lastTagsSearchResults?.let { searchResults ->
            searchResults.results.forEach { result ->
                if(result.hasExactMatches()) {
                    result.exactMatches.forEach { toggleTagAffiliation(it, tagsOnEntry) }
                }
                else {
                    result.allMatches.filterNotNull().forEach { toggleTagAffiliation(it, tagsOnEntry) }
                }
            }
        }
    }

    private fun toggleTagAffiliation(tag: Tag, tagsOnEntry: MutableCollection<Tag>) {
        if (tagsOnEntry.contains(tag)) {
            tagsOnEntry.remove(tag)
        }
        else {
            tagsOnEntry.add(tag)
        }
    }

}