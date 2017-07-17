package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IDialogService


class TagsOnEntryListPresenter(tagsListView: ITagsListView, searchEngine: ISearchEngine, tagService: TagService, searchResultsUtil: TagsSearchResultsUtil, dialogService: IDialogService)
    : TagsListPresenterBase(tagsListView, searchEngine, tagService, searchResultsUtil, dialogService) {


    init {
        CommonComponent.component.inject(this)
    }

    fun getButtonStateForSearchResult(): TagsSearcherButtonState {
        return searchResultsUtil.getButtonStateForSearchResult(lastTagsSearchResults)
    }

    fun toggleTagsOnEntry(tagsOnEntry: MutableList<Tag>) {
        lastTagsSearchResults?.let { searchResults ->
            searchResults.results.forEach { result ->
                if(result.hasExactMatches()) {
                    result.exactMatches.forEach { toggleTagAffiliation(it, tagsOnEntry) }
                }
                else {
                    result.allMatches.forEach { toggleTagAffiliation(it, tagsOnEntry) }
                }
            }
        }
    }

    private fun toggleTagAffiliation(tag: Tag, tagsOnEntry: MutableList<Tag>) {
        if (tagsOnEntry.contains(tag)) {
            tagsOnEntry.remove(tag)
        }
        else {
            tagsOnEntry.add(tag)
        }
    }

}