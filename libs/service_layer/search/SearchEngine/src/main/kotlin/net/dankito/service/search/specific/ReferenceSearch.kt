package net.dankito.service.search.specific


import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class ReferenceSearch(searchTerm: String = Search.EmptySearchTerm,
                      val mustHaveThisSeries: Series? = null, val mustHaveTheseFiles: Collection<FileLink> = mutableListOf(),
                      completedListener: (List<Source>) -> Unit)
    : SearchWithCollectionResult<Source>(searchTerm, completedListener) {

}
