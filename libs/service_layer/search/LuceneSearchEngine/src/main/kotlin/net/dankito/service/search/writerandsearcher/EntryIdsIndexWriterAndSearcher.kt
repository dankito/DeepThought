package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Entry
import net.dankito.service.data.EntryService
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.EntriesSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.index.Term
import org.apache.lucene.search.SortField
import org.apache.lucene.search.WildcardQuery


/**
 * An Index that only contains entry ids so that searching for all entries (e.g. on app start) is a little bit faster than searching in big EntryIndexWriterAndSearcher
 */
class EntryIdsIndexWriterAndSearcher(entryService: EntryService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Entry>(entryService, eventBus, osHelper, threadPool) {

    companion object {
        private val MaxEntriesSearchResults = 1000000 // e.g. for AllEntriesCalculatedTag all entries must be returned
    }


    override fun getDirectoryName(): String {
        return "entry_ids"
    }

    override fun getIdFieldName(): String {
        return FieldName.EntryIdsId
    }


    override fun addEntityFieldsToDocument(entity: Entry, doc: Document) {
        // nothing to do here, entry's id is already added in parent to doc

        doc.add(LongField(FieldName.EntryIdsCreated, entity.createdOn.time, Field.Store.YES))
    }


    fun searchEntryIds(search: EntriesSearch, termsToFilterFor: List<String>) {
        val query = WildcardQuery(Term(getIdFieldName(), "*"))

        executeQueryForSearchWithCollectionResult(search, query, Entry::class.java, MaxEntriesSearchResults, SortOption(FieldName.EntryCreated, SortOrder.Descending, SortField.Type.LONG))
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(entryChanged: EntryChanged) {
                handleEntityChange(entryChanged)
            }

        }
    }

}