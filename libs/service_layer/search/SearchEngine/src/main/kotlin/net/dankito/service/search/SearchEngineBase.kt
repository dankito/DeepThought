package net.dankito.service.search

import net.dankito.service.search.specific.EntriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.utils.IThreadPool




abstract class SearchEngineBase(protected val threadPool: IThreadPool) : ISearchEngine {

    private var isInitialized = false

    private val initializationListeners = mutableSetOf<() -> Unit>()


    override fun searchEntries(search: EntriesSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, " ")

        threadPool.runAsync { searchEntries(search, termsToSearchFor) }
    }

    abstract fun searchEntries(search: EntriesSearch, termsToSearchFor: List<String>)

    override fun searchTags(search: TagsSearch) {
        if (search.searchTerm.isNullOrBlank())
            searchTags(search, ArrayList<String>())
        else {
            val tagNamesToFilterFor = getSingleSearchTerms(search.searchTerm, ",")

            threadPool.runAsync { searchTags(search, tagNamesToFilterFor) }
        }
    }

    abstract fun searchTags(search: TagsSearch, termsToSearchFor: List<String>)


    private fun getSingleSearchTerms(overallSearchTerm: String, separator: String): List<String> {
        // make overallSearchTerm lower case, split it at all separators and trim resulting single search terms
        return overallSearchTerm.toLowerCase().split(separator).map { it.trim() }.dropLastWhile { it.isEmpty() }
    }


    override fun addInitializationListener(listener: () -> Unit) {
        if(isInitialized) {
            callInitializationListener(listener)
        }
        else {
            initializationListeners.add(listener)
        }
    }

    protected fun searchEngineInitialized() {
        isInitialized = true

        for(listener in HashSet<() -> Unit>(initializationListeners)) {
            callInitializationListener(listener)
        }

        initializationListeners.clear()
    }

    private fun callInitializationListener(listener: () -> Unit) {
        listener()
    }

}