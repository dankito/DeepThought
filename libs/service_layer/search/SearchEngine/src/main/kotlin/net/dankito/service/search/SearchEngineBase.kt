package net.dankito.service.search

import net.dankito.service.search.specific.*
import net.dankito.utils.IThreadPool




abstract class SearchEngineBase(protected val threadPool: IThreadPool) : ISearchEngine {

    companion object {
        const val TagsSearchTermSeparator = ","
    }


    private var isInitialized = false

    private val initializationListeners = mutableSetOf<() -> Unit>()


    override fun searchEntries(search: EntriesSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, " ")

        threadPool.runAsync { searchEntries(search, termsToSearchFor) }
    }

    abstract fun searchEntries(search: EntriesSearch, termsToSearchFor: List<String>)


    override fun searchTags(search: TagsSearch) {
        var tagNamesToFilterFor: List<String> = ArrayList<String>()

        if (search.searchTerm.isNullOrBlank() == false) {
            tagNamesToFilterFor = getSingleSearchTerms(search.searchTerm, TagsSearchTermSeparator)
        }

        search.results.tagNamesToSearchFor = tagNamesToFilterFor

        threadPool.runAsync { searchTags(search, tagNamesToFilterFor) }
    }

    abstract fun searchTags(search: TagsSearch, termsToSearchFor: List<String>)


    override fun searchFilteredTags(search: FilteredTagsSearch) {
        var tagNamesToFilterFor: List<String> = ArrayList<String>()

        if (search.searchTerm.isNullOrBlank() == false) {
            tagNamesToFilterFor = getSingleSearchTerms(search.searchTerm, TagsSearchTermSeparator)
        }

        threadPool.runAsync { searchFilteredTags(search, tagNamesToFilterFor) }
    }

    abstract fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>)


    override fun searchReferences(search: ReferenceSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, " ")

        threadPool.runAsync { searchReferences(search, termsToSearchFor) }
    }

    abstract fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>)


    override fun searchSeries(search: SeriesSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, " ")

        threadPool.runAsync { searchSeries(search, termsToSearchFor) }
    }

    abstract fun searchSeries(search: SeriesSearch, termsToSearchFor: List<String>)


    override fun searchReadLaterArticles(search: ReadLaterArticleSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, " ")

        threadPool.runAsync { searchReadLaterArticles(search, termsToSearchFor) }
    }

    abstract fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>)


    private fun getSingleSearchTerms(overallSearchTerm: String, separator: String): List<String> {
        // make overallSearchTerm lower case, split it at all separators and trim resulting single search terms
        return overallSearchTerm.toLowerCase().split(separator).map { it.trim() }.filter { it.isNullOrBlank() == false }.dropLastWhile { it.isEmpty() }
    }


    override fun addInitializationListener(listener: () -> Unit) {
        if(isInitialized) {
            callInitializationListener(listener)
        }
        else {
            initializationListeners.add(listener)
        }
    }

    protected open fun searchEngineInitialized() {
        isInitialized = true

        for(listener in HashSet<() -> Unit>(initializationListeners)) {
            threadPool.runAsync { callInitializationListener(listener) }
        }

        initializationListeners.clear()
    }

    private fun callInitializationListener(listener: () -> Unit) {
        listener()
    }

}