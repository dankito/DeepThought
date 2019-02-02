package net.dankito.service.search.specific


import net.dankito.deepthought.model.*
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult
import net.dankito.service.search.util.SortOption


class ItemsSearch(searchTerm: String = Search.EmptySearchTerm,
                  val searchInContent: Boolean = true, val searchInSummary: Boolean = true,
                  val searchInTags: Boolean = true,
                  val searchInSource: Boolean = true, val searchInFiles: Boolean = true,
                  val searchOnlyItemsWithoutTags: Boolean = false,
                  val itemsMustHaveTheseTags: Collection<Tag> = mutableListOf(),
                  val itemsMustHaveThisSource: Source? = null, val itemsMustHaveThisSeries: Series? = null,
                  val itemsMustHaveTheseFiles: Collection<FileLink> = mutableListOf(),
                  val sortOptions: List<SortOption> = emptyList(),
                  completedListener: (List<Item>) -> Unit) : SearchWithCollectionResult<Item>(searchTerm, completedListener) {

    fun isSearchingForItemIds(): Boolean {
        return searchTerm.isBlank() && searchOnlyItemsWithoutTags == false && itemsMustHaveTheseTags.isEmpty() &&
                itemsMustHaveThisSource == null && itemsMustHaveThisSeries == null && itemsMustHaveTheseFiles.isEmpty() &&
                (sortOptions.isEmpty() || isOnlySortingByItemCreated(sortOptions))
    }

    private fun isOnlySortingByItemCreated(sortOptions: List<SortOption>): Boolean {
        return sortOptions.size == 1 && sortOptions[0].property == "item_created" // this is bad, we cannot reference FieldName.ItemCreated here
    }

}
