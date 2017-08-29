package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.Entry
import net.dankito.service.search.FieldName
import net.dankito.utils.IThreadPool
import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.slf4j.LoggerFactory


class FilteredTagsLazyLoadingLuceneSearchResultsList(entityManager: IEntityManager, searcher: IndexSearcher, hits: Array<ScoreDoc>, threadPool: IThreadPool)
    : LazyLoadingLuceneSearchResultsList<Entry>(entityManager, searcher, Entry::class.java, FieldName.EntryId, hits, threadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(FilteredTagsLazyLoadingLuceneSearchResultsList::class.java)
    }


    var tagIdsOnResultEntries = HashSet<String>()


    init {
        try {
            this.entityIds = retrieveEntityIds(hits)
        } catch (ex: Exception) {
            log.error("Could not retrieve results from hits", ex)
        }

    }

    private fun retrieveEntityIds(hits: Array<ScoreDoc>): MutableCollection<String> {
        val ids = LinkedHashSet<String>()

        try {
            for(index in hits.indices) {
                val entityId = getEntityIdForIndex(index)

                ids.add(entityId)
            }
        } catch (ex: Exception) {
            log.error("Could not get all Entity IDs from Lucene Search Result", ex)
        }

        return ids
    }


    override fun getEntityIdFromHit(hitDoc: Document): String {
        val entityId = super.getEntityIdFromHit(hitDoc)

        hitDoc.getFields(FieldName.EntryTagsIds).forEach { tagIdsOnResultEntries.add(it.stringValue()) }

        return entityId
    }

}