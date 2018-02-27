package net.dankito.service.search.results

import net.dankito.synchronization.database.IEntityManager
import net.dankito.deepthought.model.Item
import net.dankito.service.search.FieldName
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import org.apache.lucene.document.Document
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.slf4j.LoggerFactory
import java.util.*


class FilteredTagsLazyLoadingLuceneSearchResultsList(entityManager: IEntityManager, searcher: IndexSearcher, hits: Array<ScoreDoc>, osHelper: OsHelper, threadPool: IThreadPool)
    : LazyLoadingLuceneSearchResultsList<Item>(entityManager, searcher, Item::class.java, FieldName.ItemId, hits, osHelper, threadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(FilteredTagsLazyLoadingLuceneSearchResultsList::class.java)
    }


    var tagIdsOnResultItems = HashSet<String>()

    // yeah, i know, never call overwritable methods in a constructor, but LazyLoadingLuceneSearchResultsList does so by calling retrieveEntityFromDatabaseAndCacheIfNotRetrievedYet()
    // -> handle it by temporarily storing ids in tagIdsOnResultItemsCollectedBeforeCallToInit
    private var tagIdsOnResultItemsCollectedBeforeCallToInit: List<String>? = null


    init {
        tagIdsOnResultItemsCollectedBeforeCallToInit?.let {
            tagIdsOnResultItems.addAll(it)
            tagIdsOnResultItemsCollectedBeforeCallToInit = null
        }

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

        val itemTagIds = hitDoc.getFields(FieldName.ItemTagsIds).map { it.stringValue() }

        if(tagIdsOnResultItems == null) {
            tagIdsOnResultItemsCollectedBeforeCallToInit = itemTagIds
        }
        else {
            tagIdsOnResultItems.addAll(itemTagIds)
        }

        hitDoc.getFields(FieldName.ItemTagsIds).forEach {
        }

        return entityId
    }

}