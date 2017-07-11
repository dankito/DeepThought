package net.dankito.service.search.results

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.Entry
import net.dankito.service.search.FieldName
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc


class FilteredTagsLazyLoadingLuceneSearchResultsList(entityManager: IEntityManager, searcher: IndexSearcher, hits: Array<ScoreDoc>)
    : LazyLoadingLuceneSearchResultsList<Entry>(entityManager, searcher, Entry::class.java, FieldName.EntryId, hits) {

    lateinit var tagIdsOnResultEntries: HashSet<String>


    override fun getEntityIdFromHit(hits: Array<ScoreDoc>, index: Int): String {
        val hitDoc = searcher.doc(hits[index].doc)

        val entityId = hitDoc.getField(idFieldName).stringValue()

        tagIdsOnResultEntries = HashSet()

        hitDoc.getFields(FieldName.EntryTagsIds).forEach { tagIdsOnResultEntries.add(it.stringValue()) }

        return entityId
    }

}