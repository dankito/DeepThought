package net.dankito.service.search

import net.dankito.service.search.specific.EntriesSearch
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.service.search.specific.TagsSearch


interface ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchEntries(search: EntriesSearch)

    fun searchTags(search: TagsSearch)

//    fun getEntriesForTagAsync(tag: Tag, listener: SearchCompletedListener<Collection<Entry>>)

//    fun findAllEntriesHavingTheseTags(tagsToFilterFor: Collection<Tag>, listener: SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>)
//    fun findAllEntriesHavingTheseTags(tagsToFilterFor: Collection<Tag>, searchTerm: String, listener: SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>)

    fun searchReferences(search: ReferenceSearch)

//    fun searchFiles(search: FilesSearch)
//
//
//    fun close()

}
