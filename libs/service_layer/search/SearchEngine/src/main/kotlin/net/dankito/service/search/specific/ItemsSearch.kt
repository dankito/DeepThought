package net.dankito.service.search.specific


import net.dankito.deepthought.model.*
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class ItemsSearch(searchTerm: String = Search.EmptySearchTerm,
                  val searchInContent: Boolean = true, val searchInSummary: Boolean = true,
                  val searchInTags: Boolean = true,
                  val searchInSource: Boolean = true, val searchInFiles: Boolean = true,
                  val searchOnlyItemsWithoutTags: Boolean = false,
                  val itemsMustHaveTheseTags: Collection<Tag> = mutableListOf(),
                  val itemsMustHaveThisSource: Source? = null, val itemsMustHaveThisSeries: Series? = null,
                  val itemsMustHaveTheseFiles: Collection<DeepThoughtFileLink> = mutableListOf(),
                  completedListener: (List<Item>) -> Unit) : SearchWithCollectionResult<Item>(searchTerm, completedListener) {

    fun isSearchingForItemIds(): Boolean {
        return searchTerm.isBlank() && searchOnlyItemsWithoutTags == false && itemsMustHaveTheseTags.isEmpty() &&
                itemsMustHaveThisSource == null && itemsMustHaveThisSeries == null && itemsMustHaveTheseFiles.isEmpty()
    }

}
