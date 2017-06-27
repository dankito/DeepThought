package net.dankito.service.search

import net.dankito.service.search.specific.EntriesSearch
import net.dankito.service.search.specific.TagsSearch


interface ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchEntries(search: EntriesSearch)

    fun searchTags(search: TagsSearch)

}
