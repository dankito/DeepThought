package net.dankito.service.search.specific


import net.dankito.deepthought.model.Tag
import java.util.*


class TagsSearchResult {

    var searchTerm: String
        private set

    private var hasExactMatches: Boolean = false

    var exactMatches: List<Tag> = ArrayList()
        private set

    private var singleMatchProperty: Tag? = null

    var allMatches: List<Tag>
        private set


    constructor(searchTerm: String, allMatches: List<Tag>) {
        this.searchTerm = searchTerm
        this.allMatches = allMatches

        if(searchTerm.isNullOrBlank() == false) { // don't call findExactMatch() for an empty search term, would load all tags
            findExactMatch(searchTerm, allMatches)
        }
    }

    constructor(searchTerm: String, allMatches: List<Tag>, exactMatches: List<Tag>) {
        this.searchTerm = searchTerm
        this.allMatches = allMatches
        this.hasExactMatches = exactMatches.isNotEmpty()
        this.exactMatches = exactMatches
    }


    private fun findExactMatch(searchTerm: String, allMatches: List<Tag>) {
        for(match in allMatches) {
            if(match != null && searchTerm == match.name.toLowerCase()) {
                this.hasExactMatches = true
                (this.exactMatches as MutableList).add(match)
            }
        }
    }

    fun hasExactMatches(): Boolean {
        return hasExactMatches
    }

    fun hasSingleMatch(): Boolean {
        return allMatchesCount == 1
    }

    fun getSingleMatch(): Tag? {
        if (hasSingleMatch()) {
            if (singleMatchProperty == null) {
                if (allMatches is List<*>)
                    singleMatchProperty = allMatches[0]
                else
                    singleMatchProperty = ArrayList(allMatches)[0]
            }

            return singleMatchProperty
        }

        return null
    }

    val allMatchesCount: Int
        get() = allMatches.size


    override fun toString(): String {
        return searchTerm + " has " + allMatchesCount + " matches, hasExactMatches = " + hasExactMatches()
    }

}
