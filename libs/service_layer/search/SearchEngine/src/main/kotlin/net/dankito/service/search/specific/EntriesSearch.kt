package net.dankito.service.search.specific


import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class EntriesSearch(searchTerm: String = Search.EmptySearchTerm, val filterContent: Boolean = true, val filterAbstract: Boolean = true, val filterReference: Boolean = true,
                    val filterTags: Boolean = true, val filterOnlyEntriesWithoutTags: Boolean = false,
                    val entriesMustHaveTheseTags: MutableCollection<Tag> = mutableListOf(), val entriesMustHaveThisSource: Source? = null, val entriesMustHaveThisSeries: Series? = null,
                    completedListener: (List<Item>) -> Unit) : SearchWithCollectionResult<Item>(searchTerm, completedListener) {

    fun isSearchingForEntryIds(): Boolean {
        return searchTerm == Search.EmptySearchTerm && filterOnlyEntriesWithoutTags == false && entriesMustHaveTheseTags.isEmpty() &&
                entriesMustHaveThisSource == null && entriesMustHaveThisSeries == null
    }

}
