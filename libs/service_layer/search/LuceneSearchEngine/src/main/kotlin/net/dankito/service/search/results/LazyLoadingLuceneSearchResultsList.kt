package net.dankito.service.search.results

import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.model.BaseEntity
import net.dankito.service.search.util.LazyLoadingList
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc


open class LazyLoadingLuceneSearchResultsList<T : BaseEntity>(entityManager: IEntityManager, protected var searcher: IndexSearcher, resultType: Class<T>,
                            protected var idFieldName: String, private val hits: Array<ScoreDoc>, private val osHelper: OsHelper, private val threadPool: IThreadPool)
    : LazyLoadingList<T>(entityManager, resultType) {

    companion object {
        private val MaxItemsToPreloadOnStart = 6
        private val MaxItemsToPreloadWhileScrolling = 20
    }


    init {
        if(isRunningOnAndroid()) {
            preloadFirstItems()
        }
    }

    private fun preloadFirstItems() {
        // real ugly code, but this seems to be the fastest variant: Load two items upfront synchronously (otherwise they may get displayed with incomplete data) and the rest  asynchronously
        if(hits.size > 0) {
            retrieveEntityFromDatabaseAndCacheIfNotRetrievedYet(0) // load first item directly as otherwise UI may already tries to display it while it's not fully loaded yet (and therefore not all data gets displayed)

            if(hits.size > 1) {
                retrieveEntityFromDatabaseAndCacheIfNotRetrievedYet(1)
            }

            if(hits.size > 3) { // not loading item with index 2, leads to problems for Items (on fast CPUs is not fully displayed)
                preloadItemsAsync(3, MaxItemsToPreloadOnStart - 2) // preload items that for sure gonna get displayed off the UI thread
            }
        }
    }


    override val size: Int
        get() = hits.size

    override fun getEntityIdForIndex(index: Int): String {
        val hitDoc = searcher.doc(hits[index].doc)
        return getEntityIdFromHit(hitDoc)
    }

    protected open fun getEntityIdFromHit(hitDoc: Document): String {
        return hitDoc.getField(idFieldName).stringValue()
    }


    /**
     * This method is presumably called on UI thread, so load the items, that quite sure gonna get displayed next, off UI thread.
     * Provides a smoother scrolling experience.
     */
    override fun retrieveEntityFromDatabaseAndCache(index: Int): T? {
        if(index > MaxItemsToPreloadOnStart) { // MaxItemsToPreload: that many items get already loaded when initializing LazyLoadingList
            preloadItemsAsync(index + 1, MaxItemsToPreloadWhileScrolling)
        }

        return super.retrieveEntityFromDatabaseAndCache(index)
    }

    private fun preloadItemsAsync(startIndex: Int, maxItemsToPreload: Int) {
        if(isRunningOnAndroid()) {
            threadPool.runAsync {
                preloadItems(startIndex, maxItemsToPreload)
            }
        }
    }

    private fun preloadItems(startIndex: Int, maxItemsToPreload: Int) {
        val maxIndexToPreload = if(hits.size < startIndex + maxItemsToPreload) hits.size else startIndex + maxItemsToPreload

        for(i in startIndex..maxIndexToPreload - 1) {
            retrieveEntityFromDatabaseAndCacheIfNotRetrievedYet(i)
        }
    }

    private fun retrieveEntityFromDatabaseAndCacheIfNotRetrievedYet(index: Int) {
        if(cachedResults[index] == null) {
            super.retrieveEntityFromDatabaseAndCache(index)
        }
    }


    private fun isRunningOnAndroid(): Boolean {
        return osHelper.isRunningOnAndroid
    }

}
