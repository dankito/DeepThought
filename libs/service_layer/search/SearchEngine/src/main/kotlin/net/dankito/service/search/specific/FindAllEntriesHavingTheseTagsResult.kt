package net.dankito.service.search.specific


import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.util.CombinedLazyLoadingList


class FindAllEntriesHavingTheseTagsResult(entriesHavingFilteredTags: List<Entry>, tagsOnEntriesContainingFilteredTags: List<Tag>) {

    var entriesHavingFilteredTags: Collection<Entry>
        protected set

    var tagsOnEntriesContainingFilteredTags: Collection<Tag>
        protected set
    protected var tagsOnEntriesContainingFilteredTagsList: List<Tag>? = null


    init {
        this.entriesHavingFilteredTags = entriesHavingFilteredTags
        this.tagsOnEntriesContainingFilteredTags = tagsOnEntriesContainingFilteredTags
    }

    val tagsOnEntriesContainingFilteredTagsCount: Int
        get() = tagsOnEntriesContainingFilteredTags.size

    fun getTagsOnEntriesContainingFilteredTagsAt(index: Int): Tag? {
        if (index < 0 || index >= tagsOnEntriesContainingFilteredTagsCount)
            return null

        if (tagsOnEntriesContainingFilteredTagsList == null) {
            if (tagsOnEntriesContainingFilteredTags is List<*>)
                tagsOnEntriesContainingFilteredTagsList = tagsOnEntriesContainingFilteredTags as List<Tag>
            else
                tagsOnEntriesContainingFilteredTagsList = CombinedLazyLoadingList(tagsOnEntriesContainingFilteredTags)
        }

        return tagsOnEntriesContainingFilteredTagsList!![index]
    }
}
