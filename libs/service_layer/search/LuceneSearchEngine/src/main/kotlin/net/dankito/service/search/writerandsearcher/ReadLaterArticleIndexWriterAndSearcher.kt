package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.messages.ReadLaterArticleChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ReadLaterArticleSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class ReadLaterArticleIndexWriterAndSearcher(readLaterArticleService: ReadLaterArticleService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<ReadLaterArticle>(readLaterArticleService, eventBus, osHelper, threadPool) {

    override fun getDirectoryName(): String {
        return "read_later_articles"
    }

    override fun getIdFieldName(): String {
        return FieldName.ReadLaterArticleId
    }


    override fun addEntityFieldsToDocument(entity: ReadLaterArticle, doc: Document) {
        val item = entity.itemExtractionResult.item

        val textToIndex = item.summaryPlainText + " " + item.contentPlainText
        if(textToIndex.isNotBlank()) { // Lucene crashes when trying to index empty strings
            doc.add(Field(FieldName.ReadLaterArticleItem, textToIndex, TextField.TYPE_NOT_STORED))
        }

        entity.itemExtractionResult.source?.let { source ->
            val sourcePreview = source.previewWithSeriesAndPublishingDate
            if(sourcePreview.isNotEmpty()) {
                doc.add(Field(FieldName.ReadLaterArticleSource, sourcePreview, TextField.TYPE_NOT_STORED))
            }
        }
    }

    fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        val query = BooleanQuery()

        addQueryForSearchTerm(termsToSearchFor, query)

        executeQueryForSearchWithCollectionResult(search, query, ReadLaterArticle::class.java,
                sortOptions = *arrayOf(SortOption(FieldName.ModifiedOn, SortOrder.Descending, SortField.Type.LONG))
        )
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<String>, query: BooleanQuery) {
        if(termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(getIdFieldName(), "*")), BooleanClause.Occur.MUST)
        }
        else {
            for(term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                termQuery.add(PrefixQuery(Term(FieldName.ReadLaterArticleItem, escapedTerm)), BooleanClause.Occur.SHOULD)

                termQuery.add(PrefixQuery(Term(FieldName.ReadLaterArticleSource, escapedTerm)), BooleanClause.Occur.SHOULD)

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