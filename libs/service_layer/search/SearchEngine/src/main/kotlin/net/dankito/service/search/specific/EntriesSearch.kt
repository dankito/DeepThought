package net.dankito.service.search.specific


import net.dankito.deepthought.model.*
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class EntriesSearch(searchTerm: String = Search.EmptySearchTerm,
                    val filterContent: Boolean = true, val filterAbstract: Boolean = true,
                    val filterTags: Boolean = true,
                    val filterReference: Boolean = true, val searchInFiles: Boolean = true,
                    val filterOnlyEntriesWithoutTags: Boolean = false,
                    val entriesMustHaveTheseTags: Collection<Tag> = mutableListOf(),
                    val entriesMustHaveThisSource: Source? = null, val entriesMustHaveThisSeries: Series? = null,
                    val entriesMustHaveTheseFiles: Collection<FileLink> = mutableListOf(),
                    completedListener: (List<Item>) -> Unit) : SearchWithCollectionResult<Item>(searchTerm, completedListener) {

    fun isSearchingForEntryIds(): Boolean {
        return searchTerm.isBlank() && filterOnlyEntriesWithoutTags == false && entriesMustHaveTheseTags.isEmpty() &&
                entriesMustHaveThisSource == null && entriesMustHaveThisSeries == null && entriesMustHaveTheseFiles.isEmpty()
    }

}
