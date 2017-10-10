package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag
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
        if (searchResults == null || searchResults.overAllSearchTerm.isBlank()) {
            return TagsSearcherButtonState.DISABLED
        }

        if(searchResults.getSearchTermsWithoutMatches().size == searchResults.tagNamesToSearchFor.size ||
                (searchResults.getSearchTermsWithoutMatches().size == searchResults.tagNamesToSearchFor.size - 1 && searchResults.lastResult?.hasExactMatches() == false)) { // last result contains more then just the exact matches
            return TagsSearcherButtonState.CREATE_TAG
        }


        // the basic idea is - if there's at least one not added tag, we show 'Add'.
        if(containsOnlyAddedTags(tagsOnEntry, searchResults)) {
            return TagsSearcherButtonState.REMOVE_TAGS
        }

        // as we have excluded all other cases above, this is the only one that remains
        return TagsSearcherButtonState.ADD_TAGS
    }

    private fun containsOnlyAddedTags(tagsOnEntry: Collection<Tag>, searchResults: TagsSearchResults): Boolean {
        if(tagsOnEntry.isEmpty() || searchResults.getAllMatches().size > (tagsOnEntry.size + searchResults.getSearchTermsWithoutMatches().size)) {
            return false
        }

        val remainingMatches = ArrayList(searchResults.getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible())
        remainingMatches.removeAll(tagsOnEntry)

        return remainingMatches.size <= searchResults.getSearchTermsWithoutMatches().size
    }

}