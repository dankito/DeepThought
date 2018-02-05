package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.service.data.ItemService
import net.dankito.service.data.messages.ItemChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.FieldValue
import net.dankito.service.search.FieldValue.NoTagsFieldValue
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class ItemIndexWriterAndSearcher(itemService: ItemService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Item>(itemService, eventBus, osHelper, threadPool) {

    companion object {
        private val MaxEntriesSearchResults = 1000000 // e.g. for AllEntriesCalculatedTag all items must be returned
    }


    override fun getDirectoryName(): String {
        return "items"
    }

    override fun getIdFieldName(): String {
        return FieldName.ItemId
    }


    override fun addEntityFieldsToDocument(entity: Item, doc: Document) {
        val contentPlainText = entity.contentPlainText
        val abstractPlainText = entity.abstractPlainText

        doc.add(Field(FieldName.ItemSummary, abstractPlainText, TextField.TYPE_NOT_STORED))
        doc.add(Field(FieldName.ItemContent, contentPlainText, TextField.TYPE_NOT_STORED))

        doc.add(LongField(FieldName.ItemIndex, entity.itemIndex, Field.Store.YES))

        doc.add(LongField(FieldName.ItemCreated, entity.createdOn.time, Field.Store.YES))

        if(entity.hasTags()) {
            for(tag in entity.tags.filterNotNull().filter { it.id != null }) {
                doc.add(StringField(FieldName.ItemTagsIds, tag.id, Field.Store.YES))
                doc.add(Field(FieldName.ItemTagsNames, tag.name, TextField.TYPE_NOT_STORED))
            }
        }
        else {
            doc.add(StringField(FieldName.ItemNoTags, FieldValue.NoTagsFieldValue, Field.Store.NO))
        }

        val reference = entity.source
        if(reference != null) {
            doc.add(Field(FieldName.ItemSource, reference.previewWithSeriesAndPublishingDate, TextField.TYPE_NOT_STORED))
            doc.add(StringField(FieldName.ItemSourceId, reference.id, Field.Store.YES))

            reference.series?.let { doc.add(StringField(FieldName.ItemSourceSeriesId, it.id, Field.Store.YES)) }
        }
        else {
            doc.add(StringField(FieldName.ItemNoSource, FieldValue.NoSourceFieldValue, Field.Store.NO))
        }

        if(entity.hasAttachedFiles()) {
            for(file in entity.attachedFiles.filterNotNull().filter { it.id != null }) {
                doc.add(StringField(FieldName.ItemAttachedFilesIds, file.id, Field.Store.YES))
                doc.add(StringField(FieldName.ItemAttachedFilesDetails, file.name.toLowerCase(), Field.Store.NO)) // TODO: which information should get stored for a File?
            }
        }
        else {
            doc.add(StringField(FieldName.ItemNoAttachedFiles, FieldValue.NoFilesFieldValue, Field.Store.NO))
        }

        defaultAnalyzer.setNextItemToBeAnalyzed(entity, contentPlainText, abstractPlainText)
    }


    fun searchItems(search: ItemsSearch, termsToFilterFor: List<String>) {
        val query = BooleanQuery()

        addQueryForOptions(search, query)

        if(search.isInterrupted)
            return

        addQueryForSearchTerm(termsToFilterFor, query, search)

        executeQueryForSearchWithCollectionResult(search, query, Item::class.java, MaxEntriesSearchResults, SortOption(FieldName.ItemCreated, SortOrder.Descending, SortField.Type.LONG))
    }

    private fun addQueryForOptions(search: ItemsSearch, query: BooleanQuery) {
        if(search.searchOnlyItemsWithoutTags) {
            query.add(TermQuery(Term(FieldName.ItemNoTags, NoTagsFieldValue)), BooleanClause.Occur.MUST)
        }

        if(search.itemsMustHaveTheseTags.isNotEmpty()) {
            val filterEntriesQuery = BooleanQuery()
            for(tag in search.itemsMustHaveTheseTags.filterNotNull().filter { it.id != null }) {
                filterEntriesQuery.add(TermQuery(Term(FieldName.ItemTagsIds, tag.id)), BooleanClause.Occur.MUST)
            }

            query.add(filterEntriesQuery, BooleanClause.Occur.MUST)
        }

        search.itemsMustHaveThisSource?.id?.let { referenceId ->
            val filterReferenceQuery = BooleanQuery()
            filterReferenceQuery.add(TermQuery(Term(FieldName.ItemSourceId, referenceId)), BooleanClause.Occur.MUST)

            query.add(filterReferenceQuery, BooleanClause.Occur.MUST)
        }

        search.itemsMustHaveThisSeries?.id?.let { seriesId ->
            val filterSeriesQuery = BooleanQuery()
            filterSeriesQuery.add(TermQuery(Term(FieldName.ItemSourceSeriesId, seriesId)), BooleanClause.Occur.MUST)

            query.add(filterSeriesQuery, BooleanClause.Occur.MUST)
        }

        if(search.itemsMustHaveTheseFiles.isNotEmpty()) {
            val filterEntriesQuery = BooleanQuery()
            for(file in search.itemsMustHaveTheseFiles.filterNotNull().filter { it.id != null }) {
                filterEntriesQuery.add(TermQuery(Term(FieldName.ItemAttachedFilesIds, file.id)), BooleanClause.Occur.MUST)
            }

            query.add(filterEntriesQuery, BooleanClause.Occur.MUST)
        }
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<String>, query: BooleanQuery, search: ItemsSearch) {
        if(termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(FieldName.ItemId, "*")), BooleanClause.Occur.MUST)
        }
        else {
            for(term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                if(search.searchInContent) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemContent, escapedTerm)), BooleanClause.Occur.SHOULD)
                }
                if(search.searchInSummary) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemSummary, escapedTerm)), BooleanClause.Occur.SHOULD)
                }
                if(search.searchInSource) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemSource, escapedTerm)), BooleanClause.Occur.SHOULD)
                }
                if(search.searchInTags) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemTagsNames, escapedTerm)), BooleanClause.Occur.SHOULD)
                }
                if(search.searchInFiles) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemAttachedFilesDetails, escapedTerm)), BooleanClause.Occur.SHOULD)
                }

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(itemChanged: ItemChanged) {
                handleEntityChange(itemChanged)
            }

        }
    }

}