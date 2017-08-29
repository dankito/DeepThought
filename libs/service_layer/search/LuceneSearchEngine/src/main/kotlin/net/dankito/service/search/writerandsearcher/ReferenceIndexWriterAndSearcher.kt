package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Reference
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.IThreadPool
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class ReferenceIndexWriterAndSearcher(referenceService: ReferenceService, eventBus: IEventBus, threadPool: IThreadPool) : IndexWriterAndSearcher<Reference>(referenceService, eventBus, threadPool) {

    override fun getDirectoryName(): String {
        return "references"
    }

    override fun getIdFieldName(): String {
        return FieldName.ReferenceId
    }


    override fun addEntityFieldsToDocument(entity: Reference, doc: Document) {
        if(entity.title.isNotEmpty()) { // Lucene crashes when trying to index empty strings
            doc.add(Field(FieldName.ReferenceTitle, entity.title, TextField.TYPE_NOT_STORED))
        }
        if(entity.subTitle.isNullOrEmpty() == false) {
            doc.add(Field(FieldName.ReferenceSubTitle, entity.title, TextField.TYPE_NOT_STORED))
        }

        entity.series?.let { series ->
            if(series.title.isNotEmpty()) {
                // for an not analyzed String it's important to index it lower case as only then lower case search finds it
                doc.add(StringField(FieldName.ReferenceSeries, series.title.toLowerCase(), Field.Store.NO))
            }
        }

        entity.issue?.let { issue ->
            if(issue.isNotEmpty()) {
                doc.add(StringField(FieldName.ReferenceIssueOrPublishingDate, issue.toLowerCase(), Field.Store.NO))
            }
        }

        entity.publishingDate?.let { publishingDate -> doc.add(LongField(FieldName.ReferencePublishingDate, publishingDate.time, Field.Store.YES)) }
    }

    fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>) {
        val query = BooleanQuery()

        addQueryForSearchTerm(termsToSearchFor, query, search)

        executeQueryForSearchWithCollectionResult(search, query, Reference::class.java, sortOptions = *arrayOf(
                SortOption(FieldName.ReferenceSeries, SortOrder.Ascending, SortField.Type.STRING),
                SortOption(FieldName.ReferencePublishingDate, SortOrder.Descending, SortField.Type.LONG),
                SortOption(FieldName.ReferenceTitle, SortOrder.Ascending, SortField.Type.STRING)
            )
        )
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<String>, query: BooleanQuery, search: ReferenceSearch) {
        if (termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST)
        }
        else {
            for (term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                termQuery.add(PrefixQuery(Term(FieldName.ReferenceTitle, escapedTerm)), BooleanClause.Occur.SHOULD)
                termQuery.add(PrefixQuery(Term(FieldName.ReferenceSubTitle, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReferenceSeries, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReferencePublishingDate, escapedTerm)), BooleanClause.Occur.SHOULD)

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(referenceChanged: ReferenceChanged) {
                handleEntityChange(referenceChanged)
            }

        }
    }

}