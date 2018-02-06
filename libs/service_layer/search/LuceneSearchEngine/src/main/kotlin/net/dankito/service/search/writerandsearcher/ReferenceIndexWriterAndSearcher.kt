package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Source
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.messages.SourceChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.FieldValue
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import java.text.SimpleDateFormat


class ReferenceIndexWriterAndSearcher(referenceService: ReferenceService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Source>(referenceService, eventBus, osHelper, threadPool) {

    companion object {
        private val PublishingDateIndexFormat = SimpleDateFormat("dd MM yyyy")
    }


    override fun getDirectoryName(): String {
        return "sources"
    }

    override fun getIdFieldName(): String {
        return FieldName.ReferenceId
    }


    override fun addEntityFieldsToDocument(entity: Source, doc: Document) {
        if(entity.title.isNotEmpty()) { // Lucene crashes when trying to index empty strings
            doc.add(Field(FieldName.ReferenceTitle, entity.title, TextField.TYPE_NOT_STORED))
        }
        if(entity.subTitle.isNullOrEmpty() == false) {
            doc.add(Field(FieldName.ReferenceSubTitle, entity.subTitle, TextField.TYPE_NOT_STORED))
        }

        entity.series?.let { series ->
            doc.add(StringField(FieldName.ReferenceSeriesId, series.id, Field.Store.YES))

            if(series.title.isNotEmpty()) {
                doc.add(Field(FieldName.ReferenceSeries, series.title, TextField.TYPE_NOT_STORED))
            }
        }

        entity.issue?.let { issue ->
            if(issue.isNotEmpty()) {
                doc.add(StringField(FieldName.ReferenceIssue, issue.toLowerCase(), Field.Store.NO))
            }
        }

        indexPublishingDate(entity, doc)

        if(entity.hasAttachedFiles()) {
            for(file in entity.attachedFiles.filterNotNull().filter { it.id != null }) {
                doc.add(StringField(FieldName.ReferenceAttachedFilesIds, file.id, Field.Store.YES))
                doc.add(StringField(FieldName.ReferenceAttachedFilesDetails, file.name.toLowerCase(), Field.Store.NO)) // TODO: which information should get stored for a File?
            }
        }
        else {
            doc.add(StringField(FieldName.ReferenceNoAttachedFiles, FieldValue.NoFilesFieldValue, Field.Store.NO))
        }
    }

    private fun indexPublishingDate(entity: Source, doc: Document) {
        entity.publishingDate?.let { publishingDate -> doc.add(LongField(FieldName.ReferencePublishingDate, publishingDate.time, Field.Store.YES)) }

        val publishingDateString = if(entity.publishingDateString != null) entity.publishingDateString
        else if(entity.publishingDate != null) PublishingDateIndexFormat.format(entity.publishingDate)
        else ""

        if(publishingDateString.isNullOrBlank() == false) {
            doc.add(Field(FieldName.ReferencePublishingDateString, publishingDateString, TextField.TYPE_NOT_STORED))
        }
    }


    fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>) {
        val query = BooleanQuery()

        addQueryForOptions(search, query)

        addQueryForSearchTerm(termsToSearchFor, query, search)

        executeQueryForSearchWithCollectionResult(search, query, Source::class.java, sortOptions = *arrayOf(
                SortOption(FieldName.ReferenceSeries, SortOrder.Ascending, SortField.Type.STRING),
                SortOption(FieldName.ReferencePublishingDate, SortOrder.Descending, SortField.Type.LONG),
                SortOption(FieldName.ReferenceTitle, SortOrder.Ascending, SortField.Type.STRING)
            )
        )
    }

    private fun addQueryForOptions(search: ReferenceSearch, query: BooleanQuery) {
        search.mustHaveThisSeries?.let { series ->
            val searchSeriesQuery = BooleanQuery()
            searchSeriesQuery.add(TermQuery(Term(FieldName.ReferenceSeriesId, series.id)), BooleanClause.Occur.MUST)

            query.add(searchSeriesQuery, BooleanClause.Occur.MUST)
        }

        if(search.mustHaveTheseFiles.isNotEmpty()) {
            val searchItemsQuery = BooleanQuery()
            for(file in search.mustHaveTheseFiles.filterNotNull().filter { it.id != null }) {
                searchItemsQuery.add(TermQuery(Term(FieldName.ReferenceAttachedFilesIds, file.id)), BooleanClause.Occur.MUST)
            }

            query.add(searchItemsQuery, BooleanClause.Occur.MUST)
        }
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<String>, query: BooleanQuery, search: ReferenceSearch) {
        if(termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST)
        }
        else {
            for(term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                termQuery.add(PrefixQuery(Term(FieldName.ReferenceTitle, escapedTerm)), BooleanClause.Occur.SHOULD)
                termQuery.add(PrefixQuery(Term(FieldName.ReferenceSubTitle, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReferenceSeries, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(WildcardQuery(Term(FieldName.ReferenceIssue, "*$escapedTerm*")), BooleanClause.Occur.SHOULD)
                termQuery.add(WildcardQuery(Term(FieldName.ReferencePublishingDateString, "*$escapedTerm*")), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReferenceAttachedFilesDetails, escapedTerm)), BooleanClause.Occur.SHOULD)

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(sourceChanged: SourceChanged) {
                handleEntityChange(sourceChanged)
            }

        }
    }

}