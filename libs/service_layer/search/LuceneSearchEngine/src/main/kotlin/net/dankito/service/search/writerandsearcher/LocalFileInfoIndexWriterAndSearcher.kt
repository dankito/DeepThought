package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.service.data.LocalFileInfoService
import net.dankito.service.data.messages.LocalFileInfoChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.specific.LocalFileInfoSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.WildcardQuery


class LocalFileInfoIndexWriterAndSearcher(localFileInfoService: LocalFileInfoService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<LocalFileInfo>(localFileInfoService, eventBus, osHelper, threadPool) {


    override fun getDirectoryName(): String {
        return "local_file_info"
    }

    override fun getIdFieldName(): String {
        return FieldName.LocalFileInfoId
    }


    override fun addEntityFieldsToDocument(entity: LocalFileInfo, doc: Document) {
        doc.add(StringField(FieldName.LocalFileInfoFile, entity.file.id, Field.Store.YES))
        doc.add(StringField(FieldName.LocalFileInfoSyncStatus, entity.syncStatus.toString(), Field.Store.NO))
    }


    fun searchLocalFileInfo(search: LocalFileInfoSearch) {
        val query = BooleanQuery()

        search.fileId?.let { fileId ->
            query.add(TermQuery(Term(FieldName.LocalFileInfoFile, fileId)), BooleanClause.Occur.MUST)
        }

        search.hasSyncStatus?.let { hasSyncStatus ->
            query.add(TermQuery(Term(FieldName.LocalFileInfoSyncStatus, hasSyncStatus.toString())), BooleanClause.Occur.MUST)
        }

        search.doesNotHaveSyncStatus?.let { doesNotHaveSyncStatus ->
            val doesNotHaveSyncStatusQuery = BooleanQuery()

            doesNotHaveSyncStatusQuery.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST) // to get all LocalFileInfo
            doesNotHaveSyncStatusQuery.add(TermQuery(Term(FieldName.LocalFileInfoSyncStatus, doesNotHaveSyncStatus.toString())), BooleanClause.Occur.MUST_NOT) // and then filter out that ones with

            query.add(doesNotHaveSyncStatusQuery, BooleanClause.Occur.MUST)
        }

        if(search.isInterrupted) {
            return
        }

        executeQueryForSearchWithCollectionResult(search, query, LocalFileInfo::class.java)
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(seriesChanged: LocalFileInfoChanged) {
                handleEntityChange(seriesChanged)
            }

        }
    }

}