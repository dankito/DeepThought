package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.search.util.LazyLoadingList
import net.dankito.utils.IThreadPool
import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc


open class LazyLoadingLuceneSearchResultsList<T : BaseEntity>(entityManager: IEntityManager, protected var searcher: IndexSearcher, resultType: Class<T>,
                            protected var idFieldName: String, private val hits: Array<ScoreDoc>, threadPool: IThreadPool)
    : LazyLoadingList<T>(entityManager, resultType) {

    companion object {
        private val MaxItemsToPreload = 6
    }


    init {
        preloadForSureDisplayedItemsAsync(threadPool)
    }

    private fun preloadForSureDisplayedItemsAsync(threadPool: IThreadPool) {
        threadPool.runAsync {
            val maxItemsToPreload = if(hits.size < MaxItemsToPreload) hits.size else MaxItemsToPreload

            for(i in 0..maxItemsToPreload) {
                get(i) // preload items that for sure gonna get displayed
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

}
