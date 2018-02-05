package net.dankito.service.search

import net.dankito.service.search.specific.*


interface ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchItems(search: ItemsSearch)

    fun searchTags(search: TagsSearch)

    fun searchFilteredTags(search: FilteredTagsSearch)

    fun searchReferences(search: ReferenceSearch)

    fun searchSeries(search: SeriesSearch)

    fun searchReadLaterArticles(search: ReadLaterArticleSearch)

    fun searchFiles(search: FilesSearch)

    fun searchLocalFileInfo(search: LocalFileInfoSearch)


    fun close()

}
