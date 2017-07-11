package net.dankito.service.search.specific

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.Search


class FilteredTagsSearch(val tagsToFilterFor: List<Tag>, val searchTerm: String = Search.EmptySearchTerm, val completedListener: (FilteredTagsSearchResult) -> Unit)