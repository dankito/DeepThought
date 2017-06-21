package net.dankito.service.search

import net.dankito.service.search.specific.EntriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.utils.IThreadPool




abstract class SearchEngineBase(protected val threadPool: IThreadPool) : ISearchEngine {

    private var isInitialized = false

    private val initializationListeners = mutableSetOf<() -> Unit>()


    override fun searchEntries(search: EntriesSearch) {
        val termsToSearchFor = getTermsToSearchFor(search, " ")

        threadPool.runAsync { searchEntries(search, termsToSearchFor) }
    }

    abstract fun searchEntries(search: EntriesSearch, termsToSearchFor: Array<String>)

    override fun searchTags(search: TagsSearch) {
        if (search.searchTerm.isNullOrBlank())
            searchTags(search, ArrayList<String>())
        else {
            val tagNamesToFilterFor = search.searchTerm.apply { toLowerCase() }.split(",").map { it.trim() }.dropLastWhile { it.isEmpty() }

            threadPool.runAsync { searchTags(search, tagNamesToFilterFor) }
        }
    }

    abstract fun searchTags(search: TagsSearch, termsToSearchFor: Collection<String>)


    private fun getTermsToSearchFor(search: EntriesSearch, separator: String): Array<String> {
        val lowerCaseFilter = search.searchTerm.toLowerCase()
        val termsToSearchFor = lowerCaseFilter.split(separator).dropLastWhile { it.isEmpty() }.toTypedArray()
        return termsToSearchFor
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