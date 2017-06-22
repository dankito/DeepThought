package net.dankito.service.search.specific


import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.Search
import net.dankito.service.search.SearchWithCollectionResult
import java.util.*


class EntriesSearch(searchTerm: String = Search.EmptySearchTerm, val filterContent: Boolean = true, val filterAbstract: Boolean = true,
                    completedListener: (List<Entry>) -> Unit) : SearchWithCollectionResult<Entry>(searchTerm, completedListener) {

    var filterOnlyEntriesWithoutTags: Boolean = false

    var entriesMustHaveTheseTags: MutableCollection<Tag> = ArrayList()


    constructor(searchTerm: String, filterContent: Boolean, filterAbstract: Boolean, filterOnlyEntriesWithoutTags: Boolean, entriesMustHaveTheseTags: MutableCollection<Tag>, completedListener: (List<Entry>) -> Unit)
            : this(searchTerm, filterContent, filterAbstract, completedListener) {
        this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags
        this.entriesMustHaveTheseTags = entriesMustHaveTheseTags
    }


    fun addTagEntriesMustHave(tag: Tag): Boolean {
        return entriesMustHaveTheseTags.add(tag)
    }

}
