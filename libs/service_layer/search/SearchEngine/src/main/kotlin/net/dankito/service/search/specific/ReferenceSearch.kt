package net.dankito.service.search.specific


import net.dankito.deepthought.model.Reference
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class ReferenceSearch(searchTerm: String = Search.EmptySearchTerm, completedListener: (List<Reference>) -> Unit) : SearchWithCollectionResult<Reference>(searchTerm, completedListener) {

}
