package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.search.util.LazyLoadingList
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.slf4j.LoggerFactory
import java.util.*


open class LazyLoadingLuceneSearchResultsList<T : BaseEntity>(entityManager: IEntityManager, protected var searcher: IndexSearcher, resultType: Class<T>,
                            protected var idFieldName: String, hits: Array<ScoreDoc>)
    : LazyLoadingList<T>(entityManager, resultType) {

    companion object {
        private val log = LoggerFactory.getLogger(LazyLoadingLuceneSearchResultsList::class.java)
    }


    init {
        try {
            this.entityIds = retrieveEntityIds(hits)
        } catch (ex: Exception) {
            log.error("Could not retrieve results from hits", ex)
        }

    }

    protected open fun retrieveEntityIds(hits: Array<ScoreDoc>): MutableCollection<String> {
        val ids = LinkedHashSet<String>()

        try {
            for(index in hits.indices) {
                val entityId = getEntityIdFromHit(hits, index)

                ids.add(entityId)
            }
        } catch (ex: Exception) {
            log.error("Could not get all Entity IDs from Lucene Search Result", ex)
        }

        return ids
    }

    protected open fun getEntityIdFromHit(hits: Array<ScoreDoc>, index: Int): String {
        val hitDoc = searcher.doc(hits[index].doc)
        val entityId = hitDoc.getField(idFieldName).stringValue()

        return entityId
    }

}
