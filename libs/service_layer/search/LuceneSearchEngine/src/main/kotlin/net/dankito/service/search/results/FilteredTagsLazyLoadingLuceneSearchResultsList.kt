package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.Entry
import net.dankito.service.search.FieldName
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc


class FilteredTagsLazyLoadingLuceneSearchResultsList(entityManager: IEntityManager, searcher: IndexSearcher, hits: Array<ScoreDoc>)
    : LazyLoadingLuceneSearchResultsList<Entry>(entityManager, searcher, Entry::class.java, FieldName.EntryId, hits) {

    lateinit var tagIdsOnResultEntries: HashSet<String> // really bad code design as retrieveEntityIds() gets called in init {}, so tagIdsOnResultEntries is not initialized at
    // the point of time when retrieveEntityIds() / getEntityIdFromHit() gets called


    override fun retrieveEntityIds(hits: Array<ScoreDoc>): MutableCollection<String> {
        tagIdsOnResultEntries = HashSet() // has to be initialized here as when hits is an empty array, getEntityIdFromHit() never gets called -> tagIdsOnResultEntries wouldn't get initialized

        return super.retrieveEntityIds(hits)
    }

    override fun getEntityIdFromHit(hits: Array<ScoreDoc>, index: Int): String {
        val hitDoc = searcher.doc(hits[index].doc)

        val entityId = hitDoc.getField(idFieldName).stringValue()

        hitDoc.getFields(FieldName.EntryTagsIds).forEach { tagIdsOnResultEntries.add(it.stringValue()) }

        return entityId
    }

}