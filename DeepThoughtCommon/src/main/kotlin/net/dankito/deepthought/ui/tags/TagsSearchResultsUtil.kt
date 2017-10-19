package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.TagsSearchResults


class TagsSearchResultsUtil {

    fun getTagSearchResultState(tag: Tag?, results: TagsSearchResults?): TagSearchResultState {
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


    fun getButtonStateForSearchResult(searchResults: TagsSearchResults?, tagsOnEntry: Collection<Tag>): TagsSearcherButtonState {
        if(searchResults == null || searchResults.overAllSearchTerm.isBlank()) {
            return TagsSearcherButtonState.DISABLED
        }

        if(containsOnlyNotExistingTags(searchResults)) {
            return TagsSearcherButtonState.CREATE_TAG
        }

        if(containsOnlyAddedTags(tagsOnEntry, searchResults)) {
            return TagsSearcherButtonState.REMOVE_TAGS
        }

        if(containsAddedTags(tagsOnEntry, searchResults) == false) {
            return TagsSearcherButtonState.ADD_TAGS
        }

        // as we have excluded all other cases above, this is the only one that remains
        return TagsSearcherButtonState.TOGGLE_TAGS
    }

    private fun containsOnlyNotExistingTags(searchResults: TagsSearchResults): Boolean {
        return containsOnlySearchResultsWithoutMatches(searchResults) ||
                containsOnlyOneSearchResultAndThatWithoutExactMatch(searchResults)  // first result doesn't have an exact match -> show Create
    }

    private fun containsOnlySearchResultsWithoutMatches(searchResults: TagsSearchResults): Boolean {
        return searchResults.getSearchTermsWithoutMatches().size == searchResults.tagNamesToSearchFor.size
    }

    private fun containsOnlyOneSearchResultAndThatWithoutExactMatch(searchResults: TagsSearchResults): Boolean {
        if(searchResults.tagNamesToSearchFor.size == 1 && searchResults.lastResult?.hasExactMatches() == false) {
            // if there has been already entered a comma (= TagsSearchTermSeparator), also check if first result doesn't have matches
            if(searchResults.overAllSearchTerm.contains(SearchEngineBase.TagsSearchTermSeparator) == false || searchResults.lastResult?.hasMatches == false) {
                return true
            }
        }

        return false
    }

    private fun containsOnlyAddedTags(tagsOnEntry: Collection<Tag>, searchResults: TagsSearchResults): Boolean {
        if(tagsOnEntry.isEmpty() || searchResults.getRelevantMatchesSorted().size > (tagsOnEntry.size + searchResults.getSearchTermsWithoutMatches().size)) {
            return false
        }

        val remainingMatches = ArrayList(searchResults.getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible())
        remainingMatches.removeAll(tagsOnEntry)

        return remainingMatches.size <= searchResults.getSearchTermsWithoutMatches().size
    }

    private fun containsAddedTags(tagsOnEntry: Collection<Tag>, searchResults: TagsSearchResults): Boolean {
        if(tagsOnEntry.isEmpty() || searchResults.getRelevantMatchesSorted().size > (tagsOnEntry.size + searchResults.getSearchTermsWithoutMatches().size)) {
            return false
        }

        val remainingMatches = ArrayList(searchResults.getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible())
        remainingMatches.removeAll(tagsOnEntry)

        return remainingMatches.size < searchResults.getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible().size
    }

}