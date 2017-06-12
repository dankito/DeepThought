package net.dankito.service.search

import net.dankito.service.search.specific.EntriesSearch


interface ISearchEngine {

    fun addInitializationListener(listener: () -> Unit)


    fun searchEntries(search: EntriesSearch)

}
