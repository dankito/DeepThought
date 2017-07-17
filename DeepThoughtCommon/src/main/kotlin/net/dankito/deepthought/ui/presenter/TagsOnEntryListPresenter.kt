package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.search.ISearchEngine




class TagsOnEntryListPresenter(tagsListView: ITagsListView, searchEngine: ISearchEngine, searchResultsUtil: TagsSearchResultsUtil)
    : TagsListPresenterBase(tagsListView, searchEngine, searchResultsUtil) {

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