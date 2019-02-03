package net.dankito.service.search


class SearchTerm(val term: String, val match: SearchTermMatch = SearchTermMatch.Contains) {

    override fun toString(): String {
        return "$match $term"
    }

}