package net.dankito.service.search.specific


import net.dankito.synchronization.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.synchronization.search.Search
import net.dankito.synchronization.search.SearchWithCollectionResult


class SourceSearch(searchTerm: String = Search.EmptySearchTerm,
                   val mustHaveThisSeries: Series? = null, val mustHaveTheseFiles: Collection<FileLink> = mutableListOf(),
                   completedListener: (List<Source>) -> Unit)
    : SearchWithCollectionResult<Source>(searchTerm, completedListener) {

}
