package net.dankito.service.search.specific


import net.dankito.deepthought.model.Tag
import net.dankito.service.search.util.CombinedLazyLoadingList
import net.dankito.utils.extensions.sortedByStrings


class TagsSearchResults(val overAllSearchTerm: String) {


    val hasEmptySearchTerm = overAllSearchTerm.isBlank()

    var tagNamesToSearchFor: List<String> = listOf()

    var results: MutableList<TagsSearchResult> = ArrayList()
        private set

    private var allMatchesProperty: List<Tag>? = null

    private var relevantMatchesSortedProperty: List<Tag>? = null

    private var relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty: List<Tag>? = null

    private var exactOrSingleMatchesNotOfLastResultProperty: List<Tag>? = null

    private var matchesButOfLastResult: List<Tag>? = null

    private var searchTermsWithoutMatchesProperty: List<String>? = null


    fun addSearchResult(result: TagsSearchResult): Boolean {
        allMatchesProperty = null
        return results.add(result)
    }

    val relevantMatchesCount: Int
        get() {
            getRelevantMatchesSorted().size

            return 0
        }

    fun getRelevantMatchesSorted(): List<Tag> {
        relevantMatchesSortedProperty?.let { return it }

        return getAllMatches() // TODO: this is wrong
    }

    fun setRelevantMatchesSorted(relevantMatchesSorted: List<Tag>) {
        this.relevantMatchesSortedProperty = relevantMatchesSorted

        // TODO: what was that good for?
        if (results.size == 0) {
            addSearchResult(TagsSearchResult(overAllSearchTerm, relevantMatchesSorted))
        }
    }


    /**
     * Returns almost the same result as getRelevantMatchesSorted(), but from last TagsSearchResults returns only exactMatches if available. If not it returns all matches from last result as well.
     * As it's currently only used for tags filter (and therefore with not so many tags), we do calculate the result in memory and not via search engine
     */
    fun getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible(): List<Tag> {
        relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty?.let { return it }

        return determineRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible()
    }

    private fun determineRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible(): List<Tag> {
        val matches = ArrayList<Tag>()

        // actually the same applies for all other results as for last result: if there are exact matches, use these, otherwise allMatches
        results.forEach { result ->
            if(result.hasExactMatches()) {
                matches.addAll(result.exactMatches)
            }
            else if(result.hasSingleMatch()) {
                result.getSingleMatch()?.let { matches.add(it) }
            }
        }

        relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty = matches.sortedByStrings { it.name }

        return relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty!!
    }


    fun getAllMatches(): List<Tag> {
        if(allMatchesProperty == null) {
            allMatchesProperty = determineAllMatches()
        }

        return allMatchesProperty ?: listOf()
    }


    fun isExactOrSingleMatchButNotOfLastResult(tag: Tag): Boolean {
        if (hasEmptySearchTerm)
        // no exact or relevant matches
            return false
        if (results.size < 2)
        // no or only one (= last) result
            return false

        return getExactOrSingleMatchesNotOfLastResult().contains(tag)
    }

    fun isMatchButNotOfLastResult(tag: Tag): Boolean {
        if(hasEmptySearchTerm) { // no exact or relevant matches
            return false
        }
        if(results.size < 2) { // no or only one (= last) result
            return false
        }

        return matchesNotOfLastResult.contains(tag)
    }

    fun isExactMatchOfLastResult(tag: Tag): Boolean {
        if (hasEmptySearchTerm) { // no exact or relevant matches
            return false
        }
        if (hasLastResult() == false) {
            return false
        }

        lastResult?.let { lastResult ->
            return lastResult.hasExactMatches() && lastResult.exactMatches.contains(tag)
        }
        return false
    }

    fun isSingleMatchOfLastResult(tag: Tag): Boolean {
        if (hasEmptySearchTerm)
        // no exact or relevant matches
            return false
        if (hasLastResult() == false)
            return false

        val lastResult = lastResult
        return lastResult!!.hasSingleMatch() && lastResult.getSingleMatch() == tag
    }


    private fun getExactOrSingleMatchesNotOfLastResult(): List<Tag> {
        exactOrSingleMatchesNotOfLastResultProperty?.let { return it }

        val propertyValue = determineExactOrSingleMatchesNotOfLastResult()
        exactOrSingleMatchesNotOfLastResultProperty = propertyValue

        return propertyValue
    }

    private fun determineExactOrSingleMatchesNotOfLastResult(): List<Tag> {
        val nonLastResultExactOrSingleMatches = ArrayList<Tag>()

        for (i in 0..results.size - 2) {
            val result = results[i]

            nonLastResultExactOrSingleMatches.addAll(result.exactMatches)

            result.getSingleMatch()?.let { nonLastResultExactOrSingleMatches.add(it) }
        }

        return nonLastResultExactOrSingleMatches
    }

    private val matchesNotOfLastResult: List<Tag>
        get() {
            var propertyValue = matchesButOfLastResult
            if(propertyValue == null) {
                propertyValue = determineMatchesNotOfLastResult()
                matchesButOfLastResult = propertyValue
            }

            return propertyValue
        }

    private fun determineMatchesNotOfLastResult(): List<Tag> {
        val nonLastResultNotExactOrSingleMatches = ArrayList<Tag>()

        for(i in 0..results.size - 2) {
            val result = results[i]
            if(result.hasExactMatches()) {
                nonLastResultNotExactOrSingleMatches.addAll(result.exactMatches)
            }
            else if(result.hasSingleMatch()) {
                result.getSingleMatch()?.let { nonLastResultNotExactOrSingleMatches.add(it) }
            }
        }

        return nonLastResultNotExactOrSingleMatches
    }


    private fun determineAllMatches(): List<Tag> {
        val allMatches = CombinedLazyLoadingList<Tag>()

        results.forEach {
            allMatches.addAll(it.allMatches)
        }

        return allMatches
    }


    private fun hasLastResult(): Boolean {
        return results.size > 0 // no result (and therefore not last result) at all
    }

    val lastResult: TagsSearchResult?
        get() {
            if (results.size == 0) {
                return null
            }

            return results[results.size - 1]
        }


    fun getSearchTermsWithoutMatches(): List<String> {
        if(searchTermsWithoutMatchesProperty == null) {
            searchTermsWithoutMatchesProperty = determineSearchTermsWithoutMatches()
        }

        return searchTermsWithoutMatchesProperty ?: listOf()
    }

    private fun determineSearchTermsWithoutMatches(): List<String>? {
        val searchTermsWithoutMatches = ArrayList<String>()

        results.forEach { result ->
            if(result.hasMatches == false) {
                searchTermsWithoutMatches.add(result.searchTerm)
            }
        }

        return searchTermsWithoutMatches
    }


    override fun toString(): String {
        return "$overAllSearchTerm has $relevantMatchesCount results"
    }

}
