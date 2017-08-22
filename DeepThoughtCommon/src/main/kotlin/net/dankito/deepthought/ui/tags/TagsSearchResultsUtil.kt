package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.TagsSearchResults


class TagsSearchResultsUtil {

    fun getTagSearchResultState(tag: Tag, results: TagsSearchResults?): TagSearchResultState {
        if(results == null || tag == null) {
            return TagSearchResultState.DEFAULT
        }
        else if(results.isExactOrSingleMatchButNotOfLastResult(tag)) {
            return TagSearchResultState.EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT
        }
        else if(results.isMatchButNotOfLastResult(tag)) {
            return TagSearchResultState.MATCH_BUT_NOT_OF_LAST_RESULT
        }
        else if(results.isExactMatchOfLastResult(tag)) {
            return TagSearchResultState.EXACT_MATCH_OF_LAST_RESULT
        }
        else if(results.isSingleMatchOfLastResult(tag)) {
            return TagSearchResultState.SINGLE_MATCH_OF_LAST_RESULT
        }
        else {
            return TagSearchResultState.DEFAULT
        }
    }


    fun getButtonStateForSearchResult(searchResults: TagsSearchResults?): TagsSearcherButtonState {
        if (searchResults == null || searchResults.overAllSearchTerm.isBlank()) {
            return TagsSearcherButtonState.DISABLED
        }

        if(searchResults.getExactMatches().isEmpty() && searchResults.overAllSearchTerm.contains(SearchEngineBase.TagsSearchTermSeparator) == false) {
            return TagsSearcherButtonState.CREATE_TAG
        }

        return TagsSearcherButtonState.TOGGLE_TAGS
    }

}