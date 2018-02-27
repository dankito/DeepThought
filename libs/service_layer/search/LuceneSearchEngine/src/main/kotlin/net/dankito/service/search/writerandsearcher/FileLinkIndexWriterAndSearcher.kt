package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.service.data.FileService
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.FieldValue
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.FilesSearch
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class FileLinkIndexWriterAndSearcher(fileService: FileService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<DeepThoughtFileLink>(fileService, eventBus, osHelper, threadPool) {


    override fun getDirectoryName(): String {
        return "files"
    }

    override fun getIdFieldName(): String {
        return FieldName.FileId
    }


    override fun addEntityFieldsToDocument(entity: DeepThoughtFileLink, doc: Document) {
        // for an not analyzed String it's important to index it lower case as only then lower case search finds it
        doc.add(StringField(FieldName.FileUri, entity.uriString.toLowerCase(), Field.Store.NO))
        doc.add(StringField(FieldName.FileName, entity.name.toLowerCase(), Field.Store.NO))

        addBooleanFieldToDocument(FieldName.FileIsLocalFile, entity.isLocalFile, doc)

        entity.mimeType?.let { mimeType ->
            doc.add(StringField(FieldName.FileMimeType, mimeType, Field.Store.NO))
        }
        doc.add(StringField(FieldName.FileFileType, entity.fileType.toString().toLowerCase(), Field.Store.NO))

        doc.add(LongField(FieldName.FileFileSize, entity.fileSize, Field.Store.YES))
        entity.fileLastModified?.let { doc.add(LongField(FieldName.FileFileLastModified, it.time, Field.Store.YES)) }

        doc.add(Field(FieldName.FileDescription, entity.description, TextField.TYPE_NOT_STORED))
        doc.add(StringField(FieldName.FileSourceUri, entity.sourceUriString.toLowerCase(), Field.Store.NO))
    }


    fun searchFiles(search: FilesSearch, termsToSearchFor: List<String>) {
        val query = BooleanQuery()

        addQueryForSearchTerm(search, termsToSearchFor, query)

        addQueryForOptions(search, query)

        if(search.isInterrupted) {
            return
        }

        executeQueryForSearchWithCollectionResult(search, query, DeepThoughtFileLink::class.java, sortOptions = SortOption(FieldName.FileName, SortOrder.Ascending, SortField.Type.STRING))
    }

    private fun addQueryForSearchTerm(search: FilesSearch, termsToFilterFor: List<String>, query: BooleanQuery) {
        if(termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST)
        }
        else {
            termsToFilterFor.forEach { term ->
                val termQuery = createQueryForSearchTerm(search, term)

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }

    private fun createQueryForSearchTerm(search: FilesSearch, term: String): BooleanQuery {
        val escapedTerm = QueryParser.escape(term)
        val escapedWildcardTerm = "*" + escapedTerm + "*"
        val termQuery = BooleanQuery()

        if(search.searchUri) {
            termQuery.add(WildcardQuery(Term(FieldName.FileUri, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }
        if(search.searchName) {
            termQuery.add(WildcardQuery(Term(FieldName.FileName, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }

        if(search.searchMimeType) {
            termQuery.add(WildcardQuery(Term(FieldName.FileMimeType, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }
        if(search.searchFileType) {
            termQuery.add(WildcardQuery(Term(FieldName.FileFileType, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }

        if(search.searchDescription) {
            termQuery.add(WildcardQuery(Term(FieldName.FileDescription, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }
        if(search.searchSourceUri) {
            termQuery.add(WildcardQuery(Term(FieldName.FileSourceUri, escapedWildcardTerm)), BooleanClause.Occur.SHOULD)
        }

        return termQuery
    }

    private fun addQueryForOptions(search: FilesSearch, query: BooleanQuery) {
        when(search.fileType) {
            FilesSearch.FileType.LocalFilesOnly -> query.add(TermQuery(Term(FieldName.FileIsLocalFile, FieldValue.BooleanFieldTrueValue)), BooleanClause.Occur.MUST)
            FilesSearch.FileType.RemoteFilesOnly -> query.add(TermQuery(Term(FieldName.FileIsLocalFile, FieldValue.BooleanFieldFalseValue)), BooleanClause.Occur.MUST)
            else -> { } // nothing to add, no need to restrict query any further
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(fileChanged: FileChanged) {
                handleEntityChange(fileChanged)
            }

        }
    }

}