package net.dankito.service.search.specific

import net.dankito.deepthought.model.Tag
import net.dankito.synchronization.search.Search


class FilteredTagsSearch(val tagsToFilterFor: List<Tag>, searchTerm: String = Search.EmptySearchTerm, completedListener: (FilteredTagsSearchResult) -> Unit):
        Search<FilteredTagsSearchResult>(searchTerm, completedListener)