package net.dankito.service.search.specific


import net.dankito.deepthought.model.Tag

import java.util.ArrayList


class TagsSearchResult {

    var searchTerm: String
        protected set

    protected var hasExactMatch: Boolean = false

    var exactMatch: Tag? = null
        protected set

    protected var singleMatchProperty: Tag? = null

    var allMatches: List<Tag>
        protected set


    constructor(searchTerm: String, allMatches: List<Tag>) {
        this.searchTerm = searchTerm
        this.allMatches = allMatches

        findExactMatch(searchTerm, allMatches)
    }

    constructor(searchTerm: String, allMatches: List<Tag>, exactMatch: Tag?) {
        this.searchTerm = searchTerm
        this.allMatches = allMatches
        this.hasExactMatch = exactMatch != null
        this.exactMatch = exactMatch
    }


    protected fun findExactMatch(searchTerm: String, allMatches: List<Tag>) {
        for (match in allMatches) {
            if (searchTerm == match.name.toLowerCase()) {
                this.hasExactMatch = true
                this.exactMatch = match
                break
            }
        }
    }

    fun hasExactMatch(): Boolean {
        return hasExactMatch
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
        return searchTerm + " has " + allMatchesCount + " matches, hasExactMatch = " + hasExactMatch()
    }

}
