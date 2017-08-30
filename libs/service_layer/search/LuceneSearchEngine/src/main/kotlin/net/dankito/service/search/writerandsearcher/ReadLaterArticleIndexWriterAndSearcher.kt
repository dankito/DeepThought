package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.extensions.abstractPlainText
import net.dankito.deepthought.extensions.contentPlainText
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ReadLaterArticleSearch
import net.dankito.utils.IThreadPool
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class ReadLaterArticleIndexWriterAndSearcher(readLaterArticleService: ReadLaterArticleService, eventBus: IEventBus, threadPool: IThreadPool)
    : IndexWriterAndSearcher<ReadLaterArticle>(readLaterArticleService, eventBus, threadPool) {

    override fun getDirectoryName(): String {
        return "read_later_articles"
    }

    override fun getIdFieldName(): String {
        return FieldName.ReadLaterArticleId
    }


    override fun addEntityFieldsToDocument(entity: ReadLaterArticle, doc: Document) {
        val entry = entity.entryExtractionResult.entry

        val textToIndex = entry.abstractPlainText + " " + entry.contentPlainText
        if(textToIndex.isNotBlank()) { // Lucene crashes when trying to index empty strings
            doc.add(Field(FieldName.ReadLaterArticleEntry, textToIndex, TextField.TYPE_NOT_STORED))
        }

        entity.entryExtractionResult.reference?.let { reference ->
            if(reference.preview.isNotEmpty()) {
                doc.add(Field(FieldName.ReadLaterArticleReference, reference.preview, TextField.TYPE_NOT_STORED))
            }
        }
    }

    fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        val query = BooleanQuery()

        addQueryForSearchTerm(termsToSearchFor, query)

        executeQueryForSearchWithCollectionResult(search, query, ReadLaterArticle::class.java, sortOptions =
                SortOption(FieldName.ModifiedOn, SortOrder.Descending, SortField.Type.LONG)
        )
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<String>, query: BooleanQuery) {
        if (termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST)
        }
        else {
            for (term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                termQuery.add(PrefixQuery(Term(FieldName.ReadLaterArticleEntry, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReadLaterArticleReference, escapedTerm)), BooleanClause.Occur.SHOULD)

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(readLaterArticleChanged: ReadLaterArticleChanged) {
                handleEntityChange(readLaterArticleChanged)
            }

        }
    }

}