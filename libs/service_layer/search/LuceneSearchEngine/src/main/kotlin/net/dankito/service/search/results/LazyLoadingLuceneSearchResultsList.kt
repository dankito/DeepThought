package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.util.LazyLoadingList
import org.apache.lucene.search.*
import org.slf4j.LoggerFactory
import java.util.*


open class LazyLoadingLuceneSearchResultsList<T : BaseEntity>(entityManager: IEntityManager, private var searcher: IndexSearcher, query: Query, resultType: Class<T>,
                            private var idFieldName: String, countMaxSearchResults: Int = 1000, sortOptions: List<SortOption> = ArrayList<SortOption>(0))
    : LazyLoadingList<T>(entityManager, resultType) {

    companion object {
        private val log = LoggerFactory.getLogger(LazyLoadingLuceneSearchResultsList::class.java)
    }


    init {
        try {
            val sort = getSorting(sortOptions)
            val hits = searcher.search(query, countMaxSearchResults, sort).scoreDocs

            this.entityIds = retrieveEntityIds(hits)
        } catch (ex: Exception) {
            log.error("Could not execute Query " + query, ex)
        }

    }

    protected open fun getSorting(sortOptions: List<SortOption>): Sort {
        val sort = Sort()

        if (sortOptions.isNotEmpty()) {
            val sortFields = arrayOfNulls<SortField>(sortOptions.size)

            for(i in sortOptions.indices) {
                val (fieldName, order, type) = sortOptions[i]

                sortFields[i] = SortField(fieldName, type, order === SortOrder.Descending)
            }

            sort.setSort(*sortFields)
        }

        return sort
    }

    protected open fun retrieveEntityIds(hits: Array<ScoreDoc>): MutableCollection<String> {
        val ids = LinkedHashSet<String>()

        try {
            for(index in hits.indices) {
                val hitDoc = searcher.doc(hits[index].doc)
                val entityId = hitDoc.getField(idFieldName).stringValue()

                ids.add(entityId)
            }
        } catch (ex: Exception) {
            log.error("Could not get all Entity IDs from Lucene Search Result", ex)
        }

        return ids
    }

}
