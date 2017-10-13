package net.dankito.service.search

import net.dankito.service.search.specific.*


interface ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchEntries(search: EntriesSearch)

    fun searchTags(search: TagsSearch)

    fun searchFilteredTags(search: FilteredTagsSearch)

    fun searchReferences(search: ReferenceSearch)

    fun searchSeries(search: SeriesSearch)

    fun searchReadLaterArticles(search: ReadLaterArticleSearch)

//    fun searchFiles(search: FilesSearch)


    fun close()

}
