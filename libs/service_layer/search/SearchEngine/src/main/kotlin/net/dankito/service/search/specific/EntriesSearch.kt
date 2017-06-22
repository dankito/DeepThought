package net.dankito.service.search.specific


import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult


class EntriesSearch(searchTerm: String = Search.EmptySearchTerm, val filterContent: Boolean = true, val filterAbstract: Boolean = true,
                    val filterOnlyEntriesWithoutTags: Boolean = false, val entriesMustHaveTheseTags: MutableCollection<Tag> = mutableListOf(),
                    completedListener: (List<Entry>) -> Unit) : SearchWithCollectionResult<Entry>(searchTerm, completedListener) {


    fun addTagEntriesMustHave(tag: Tag): Boolean {
        return entriesMustHaveTheseTags.add(tag)
    }

}
