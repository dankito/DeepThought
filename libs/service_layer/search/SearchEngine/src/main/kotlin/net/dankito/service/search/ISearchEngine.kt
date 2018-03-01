package net.dankito.service.search

import net.dankito.service.search.specific.*


interface ISearchEngine : net.dankito.synchronization.search.ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchItems(search: ItemsSearch)

    fun searchTags(search: TagsSearch)

    fun searchFilteredTags(search: FilteredTagsSearch)

    fun searchSources(search: SourceSearch)

    fun searchSeries(search: SeriesSearch)

    fun searchReadLaterArticles(search: ReadLaterArticleSearch)


    fun close()

}
