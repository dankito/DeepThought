package net.dankito.service.search.specific


import net.dankito.deepthought.model.Series
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class SeriesSearch(searchTerm: String = Search.EmptySearchTerm, completedListener: (List<Series>) -> Unit) : SearchWithCollectionResult<Series>(searchTerm, completedListener) {

}
