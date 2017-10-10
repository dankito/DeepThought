package net.dankito.service.search.specific


import net.dankito.deepthought.model.Tag
import net.dankito.service.search.Search
import net.dankito.service.search.util.CombinedLazyLoadingList


class TagsSearchResults(val overAllSearchTerm: String) {

    companion object {
        val EmptySearchResults = TagsSearchResults(Search.EmptySearchTerm, ArrayList<Tag>())
    }


    val hasEmptySearchTerm = overAllSearchTerm.isNullOrBlank()

    var tagNamesToSearchFor: List<String> = listOf()

    var results: MutableList<TagsSearchResult> = ArrayList()
        private set

    private var allMatchesProperty: List<Tag>? = null

    private var relevantMatchesSortedProperty: List<Tag>? = null

    private var relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty: List<Tag>? = null

    private var exactMatchesProperty: List<Tag>? = null

    private var exactOrSingleMatchesNotOfLastResultProperty: List<Tag>? = null

    private var matchesButOfLastResult: List<Tag>? = null

    private var searchTermsWithoutMatchesProperty: List<String>? = null


    constructor(overAllSearchTerm: String, relevantMatchesSorted: List<Tag>) : this(overAllSearchTerm) {
        setRelevantMatchesSorted(relevantMatchesSorted)
    }


    fun addSearchResult(result: TagsSearchResult): Boolean {
        allMatchesProperty = null
        return results.add(result)
    }

    val relevantMatchesCount: Int
        get() {
            getRelevantMatchesSorted()?.let { it.size }

            return 0
        }

    fun getRelevantMatchesSorted(): List<Tag> {
        relevantMatchesSortedProperty?.let { return it }

        return getAllMatches()
    }

    /**
     * Returns almost the same result as getRelevantMatchesSorted(), but from last TagsSearchResults returns not allMatches but exactMatches.
     * As it's currently only used for tags filter (and therefore with not so many tags), we do calculate the result in memory and not via search engine
     */
    fun getRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible(): List<Tag> {
        relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty?.let { return it }

        return determineRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible()
    }

    private fun determineRelevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossible(): List<Tag> {
        val matches = ArrayList<Tag>()

        // from last TagSearchResult only use exact matches, but from all others all matches
        results.forEach { result ->
            if(result != lastResult) {
                matches.addAll(result.allMatches)
            }
        }

        lastResult?.let { lastResult ->
            if(lastResult.hasExactMatches()) {
                matches.addAll(lastResult.exactMatches)
            }
            else if(lastResult.hasSingleMatch()) { // ok, if lastResult doesn't have exact matches, take single match or as last resort allMatches
                lastResult.getSingleMatch()?.let { matches.add(it) }
            }
            else {
                matches.addAll(lastResult.allMatches)
            }
        }

        relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty = matches.sortedBy { it.name }

        return relevantMatchesSortedButFromLastResultOnlyExactMatchesIfPossibleProperty!!
    }

    fun setRelevantMatchesSorted(relevantMatchesSorted: List<Tag>) {
        this.relevantMatchesSortedProperty = relevantMatchesSorted

        // TODO: what was that good for?
        if (results.size == 0) {
            addSearchResult(TagsSearchResult(overAllSearchTerm, relevantMatchesSorted))
        }
    }


    fun getAllMatches(): List<Tag> {
        if(allMatchesProperty == null) {
            allMatchesProperty = determineAllMatches()
        }

        return allMatchesProperty ?: listOf()
    }


    fun getExactMatches(): List<Tag> {
        if (exactMatchesProperty == null) {
            exactMatchesProperty = determineExactMatches()
        }

        return exactMatchesProperty ?: listOf()
    }

    fun determineExactMatches(): List<Tag> {
        val exactMatches = ArrayList<Tag>()

        for(result in results) {
            exactMatches.addAll(result.exactMatches)
        }

        return exactMatches
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
        if (hasEmptySearchTerm)
        // no exact or relevant matches
            return false
        if (results.size < 2)
        // no or only one (= last) result
            return false

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

    fun isMatchOfLastResult(tag: Tag): Boolean {
        if (hasEmptySearchTerm)
        // no exact or relevant matches
            return false
        if (hasLastResult())
            return false

        return lastResult!!.allMatches.contains(tag)
    }


    private fun getExactOrSingleMatchesNotOfLastResult(): List<Tag> {
        exactOrSingleMatchesNotOfLastResultProperty?.let { return it }

        val propertyValue = determineExactOrSingleMatchesNotOfLastResult()
        exactOrSingleMatchesNotOfLastResultProperty = propertyValue

        return propertyValue
    }

    private fun determineExactOrSingleMatchesNotOfLastResult(): List<Tag> {
        val nonLastResultExactOrSingleMatches = ArrayList<Tag>()

        for (i in 0..results.size - 1 - 1) {
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

        for (i in 0..results.size - 1 - 1) {
            val result = results[i]
            if (result.hasExactMatches() == false && result.hasSingleMatch() == false)
                nonLastResultNotExactOrSingleMatches.addAll(result.allMatches)
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

    fun hasLastResultExactMatch(): Boolean {
        if (hasLastResult() == false) {
            return false
        }

        lastResult?.let { lastResult ->
            return lastResult.hasExactMatches()
        }

        return false
    }

    val lastResult: TagsSearchResult?
        get() {
            if (results.size == 0) {
                return null
            }

            return results[results.size - 1]
        }

    val exactMatchesOfLastResult: List<Tag>
        get() {
            lastResult?.let { lastResult ->
                return lastResult.exactMatches
            }

            return listOf()
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
