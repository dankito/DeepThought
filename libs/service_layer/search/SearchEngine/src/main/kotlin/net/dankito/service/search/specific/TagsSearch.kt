package net.dankito.service.search.specific


import net.dankito.deepthought.model.Tag
import net.dankito.synchronization.search.Search


class TagsSearch(searchTerm: String = Search.EmptySearchTerm, completedListener: (TagsSearchResults) -> Unit) : Search<TagsSearchResults>(searchTerm, completedListener) {


    init {
        results = TagsSearchResults(searchTerm)
    }

    protected val resultsCount: Int
        get() = results.relevantMatchesCount


    fun addResult(result: TagsSearchResult): Boolean {
        return this.results.addSearchResult(result)
    }

    fun setRelevantMatchesSorted(allMatchesSorted: List<Tag>) {
        this.results.setRelevantMatchesSorted(allMatchesSorted)
    }

}
