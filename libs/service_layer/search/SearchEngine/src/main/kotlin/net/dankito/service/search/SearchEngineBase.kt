package net.dankito.service.search

import net.dankito.service.search.specific.*
import net.dankito.utils.IThreadPool




abstract class SearchEngineBase(protected val threadPool: IThreadPool) : ISearchEngine {

    companion object {
        const val DefaultSearchTermSeparator = " "
        const val TagsSearchTermSeparator = ","
    }


    private var isInitialized = false

    private val initializationListeners = mutableSetOf<() -> Unit>()


    override fun searchEntries(search: ItemsSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, DefaultSearchTermSeparator)

        threadPool.runAsync { searchEntries(search, termsToSearchFor) }
    }

    abstract fun searchEntries(search: ItemsSearch, termsToSearchFor: List<String>)


    override fun searchTags(search: TagsSearch) {
        var tagNamesToFilterFor: List<String> = ArrayList<String>()

        if (search.searchTerm.isNullOrBlank() == false) {
            tagNamesToFilterFor = getSingleSearchTerms(search.searchTerm, TagsSearchTermSeparator, false, false)
        }

        search.results.tagNamesToSearchFor = tagNamesToFilterFor

        threadPool.runAsync { searchTags(search, tagNamesToFilterFor) }
    }

    abstract fun searchTags(search: TagsSearch, termsToSearchFor: List<String>)


    override fun searchFilteredTags(search: FilteredTagsSearch) {
        var tagNamesToFilterFor: List<String> = ArrayList<String>()

        if (search.searchTerm.isNullOrBlank() == false) {
            tagNamesToFilterFor = getSingleSearchTerms(search.searchTerm, TagsSearchTermSeparator, removeEmptySearchTerms = false)
        }

        threadPool.runAsync { searchFilteredTags(search, tagNamesToFilterFor) }
    }

    abstract fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>)


    override fun searchReferences(search: ReferenceSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, DefaultSearchTermSeparator)

        threadPool.runAsync { searchReferences(search, termsToSearchFor) }
    }

    abstract fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>)


    override fun searchSeries(search: SeriesSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, DefaultSearchTermSeparator)

        threadPool.runAsync { searchSeries(search, termsToSearchFor) }
    }

    abstract fun searchSeries(search: SeriesSearch, termsToSearchFor: List<String>)


    override fun searchReadLaterArticles(search: ReadLaterArticleSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, DefaultSearchTermSeparator)

        threadPool.runAsync { searchReadLaterArticles(search, termsToSearchFor) }
    }

    abstract fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>)


    override fun searchFiles(search: FilesSearch) {
        val termsToSearchFor = getSingleSearchTerms(search.searchTerm, DefaultSearchTermSeparator)

        threadPool.runAsync { searchFiles(search, termsToSearchFor) }
    }

    abstract  fun searchFiles(search: FilesSearch, termsToSearchFor: List<String>)


    private fun getSingleSearchTerms(overallSearchTerm: String, separator: String, lowerCaseSearchTerm: Boolean = true, removeEmptySearchTerms: Boolean = true): List<String> {
        val searchTerm = if(lowerCaseSearchTerm) overallSearchTerm.toLowerCase() else overallSearchTerm
        // make overallSearchTerm lower case, split it at all separators and trim resulting single search terms
        val singleSearchTerms = searchTerm.split(separator).map { it.trim() }

        if(removeEmptySearchTerms) {
            return singleSearchTerms.filter { it.isNullOrBlank() == false }.dropLastWhile { it.isEmpty() }
        }
        else {
            return singleSearchTerms
        }
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