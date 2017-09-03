package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.search.util.LazyLoadingList
import net.dankito.utils.IThreadPool
import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc


open class LazyLoadingLuceneSearchResultsList<T : BaseEntity>(entityManager: IEntityManager, protected var searcher: IndexSearcher, resultType: Class<T>,
                            protected var idFieldName: String, private val hits: Array<ScoreDoc>, private val threadPool: IThreadPool)
    : LazyLoadingList<T>(entityManager, resultType) {

    companion object {
        private val MaxItemsToPreload = 6
    }


    init {
        if(hits.size > 0) {
            super.retrieveEntityFromDatabaseAndCache(0) // load first item directly as otherwise UI may already tries to display it while it's not fully loaded yet (and therefore not all data gets displayed)
            preloadItemsAsync(1) // preload items that for sure gonna get displayed off the UI thread
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
        if(index > MaxItemsToPreload) { // MaxItemsToPreload: that many items get already loaded when initializing LazyLoadingList
            preloadItemsAsync(index + 1)
        }

        return super.retrieveEntityFromDatabaseAndCache(index)
    }

    private fun preloadItemsAsync(startIndex: Int) {
        threadPool.runAsync {
            preloadItems(startIndex)
        }
    }

    private fun preloadItems(startIndex: Int) {
        val maxItemsToPreload = if(hits.size < startIndex + MaxItemsToPreload) hits.size else startIndex + MaxItemsToPreload

        for(i in startIndex..maxItemsToPreload - 1) {
            super.retrieveEntityFromDatabaseAndCache(i)
        }
    }

}
