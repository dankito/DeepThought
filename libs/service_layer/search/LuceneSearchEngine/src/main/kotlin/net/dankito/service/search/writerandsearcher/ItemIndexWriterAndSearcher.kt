package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.service.data.ItemService
import net.dankito.service.data.messages.ItemChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.FieldValue
import net.dankito.service.search.FieldValue.NoTagsFieldValue
import net.dankito.service.search.SearchTerm
import net.dankito.service.search.SearchTermMatch
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.service.search.writerandsearcher.sorting.getLuceneSortOptions
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.extensions.ofMaxLength
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import java.text.SimpleDateFormat


class ItemIndexWriterAndSearcher(itemService: ItemService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Item>(itemService, eventBus, osHelper, threadPool) {

    companion object {
        @JvmStatic
        protected val MaxItemsSearchResults = 1000000 // e.g. for AllItemsCalculatedTag all items must be returned

        @JvmStatic
        protected val PublishingDateFormat = SimpleDateFormat("yyyy.MM.dd")

        @JvmStatic
        protected val MaxItemPreviewSortLength = 50
        @JvmStatic
        protected val MaxItemSummaryPreviewForSortingLength = 20
    }


    override fun getDirectoryName(): String {
        return "items"
    }

    override fun getIdFieldName(): String {
        return FieldName.ItemId
    }


    override fun addEntityFieldsToDocument(entity: Item, doc: Document) {
        val contentPlainText = entity.contentPlainText
        val summaryPlainText = entity.summaryPlainText

        doc.add(LongField(FieldName.ItemCreated, entity.createdOn.time, Field.Store.YES))

        doc.add(Field(FieldName.ItemSummary, summaryPlainText, TextField.TYPE_NOT_STORED))
        doc.add(Field(FieldName.ItemContent, contentPlainText, TextField.TYPE_NOT_STORED))

        doc.add(StringField(FieldName.ItemSummaryForSorting, summaryPlainText.ofMaxLength(MaxItemSummaryPreviewForSortingLength).toLowerCase(), Field.Store.YES))
        doc.add(StringField(FieldName.ItemPreviewForSorting, contentPlainText.trim().ofMaxLength(MaxItemPreviewSortLength).toLowerCase(), Field.Store.YES))

        doc.add(StringField(FieldName.ItemIndication, entity.indication.toLowerCase(), Field.Store.YES))

        doc.add(LongField(FieldName.ItemIndex, entity.itemIndex, Field.Store.YES))

        addTagsToDocument(entity, doc)

        addSourceToDocument(entity, doc)

        addFilesToDocument(entity, doc)

        defaultAnalyzer.setNextItemToBeAnalyzed(entity, contentPlainText, summaryPlainText)
    }

    private fun addTagsToDocument(item: Item, doc: Document) {
        if (item.hasTags()) {
            for (tag in item.tags.filterNotNull().filter { it.id != null }) {
                doc.add(StringField(FieldName.ItemTagsIds, tag.id, Field.Store.YES))
                doc.add(Field(FieldName.ItemTagsNames, tag.name, TextField.TYPE_NOT_STORED))
            }
        }
        else {
            doc.add(StringField(FieldName.ItemNoTags, NoTagsFieldValue, Field.Store.NO))
        }
    }

    private fun addSourceToDocument(item: Item, doc: Document) {
        val source = item.source

        if (source != null) {
            doc.add(StringField(FieldName.ItemSourceId, source.id, Field.Store.YES))
            doc.add(Field(FieldName.ItemSource, source.preview, TextField.TYPE_NOT_STORED))

            source.publishingDate?.let { publishingDate ->
                doc.add(LongField(FieldName.ItemSourcePublishingDate, publishingDate.time, Field.Store.YES))
                doc.add(Field(FieldName.ItemSourcePublishingDateString, PublishingDateFormat.format(publishingDate), TextField.TYPE_NOT_STORED))
            } ?: source.publishingDateString?.let { publishingDateString ->
                doc.add(Field(FieldName.ItemSourcePublishingDateString, publishingDateString, TextField.TYPE_NOT_STORED))
            }

            source.series?.let {  series ->
                doc.add(StringField(FieldName.ItemSourceSeriesId, series.id, Field.Store.YES))
                doc.add(Field(FieldName.ItemSeries, series.title, TextField.TYPE_NOT_STORED))
            }
        }
        else {
            doc.add(StringField(FieldName.ItemNoSource, FieldValue.NoSourceFieldValue, Field.Store.NO))
        }
    }

    private fun addFilesToDocument(item: Item, doc: Document) {
        if (item.hasAttachedFiles()) {
            for (file in item.attachedFiles.filterNotNull().filter { it.id != null }) {
                doc.add(StringField(FieldName.ItemAttachedFilesIds, file.id, Field.Store.YES))
                doc.add(Field(FieldName.ItemAttachedFilesDetails, file.name, TextField.TYPE_NOT_STORED)) // TODO: which information should get stored for a File?
            }
        }
        else {
            doc.add(StringField(FieldName.ItemNoAttachedFiles, FieldValue.NoFilesFieldValue, Field.Store.NO))
        }
    }


    fun searchItems(search: ItemsSearch, termsToFilterFor: List<SearchTerm>) {
        val query = BooleanQuery()

        addQueryForOptions(search, query)

        if(search.isInterrupted)
            return

        addQueryForSearchTerm(termsToFilterFor, query, search)

        executeQueryForSearchWithCollectionResult(search, query, Item::class.java, MaxItemsSearchResults, *search.getLuceneSortOptions())
    }

    private fun addQueryForOptions(search: ItemsSearch, query: BooleanQuery) {
        if(search.searchOnlyItemsWithoutTags) {
            query.add(TermQuery(Term(FieldName.ItemNoTags, NoTagsFieldValue)), BooleanClause.Occur.MUST)
        }

        if(search.itemsMustHaveTheseTags.isNotEmpty()) {
            val searchItemsQuery = BooleanQuery()
            for(tag in search.itemsMustHaveTheseTags.filterNotNull().filter { it.id != null }) {
                searchItemsQuery.add(TermQuery(Term(FieldName.ItemTagsIds, tag.id)), BooleanClause.Occur.MUST)
            }

            query.add(searchItemsQuery, BooleanClause.Occur.MUST)
        }

        search.itemsMustHaveThisSource?.id?.let { sourceId ->
            val searchSourceQuery = BooleanQuery()
            searchSourceQuery.add(TermQuery(Term(FieldName.ItemSourceId, sourceId)), BooleanClause.Occur.MUST)

            query.add(searchSourceQuery, BooleanClause.Occur.MUST)
        }

        search.itemsMustHaveThisSeries?.id?.let { seriesId ->
            val filterSeriesQuery = BooleanQuery()
            filterSeriesQuery.add(TermQuery(Term(FieldName.ItemSourceSeriesId, seriesId)), BooleanClause.Occur.MUST)

            query.add(filterSeriesQuery, BooleanClause.Occur.MUST)
        }

        if(search.itemsMustHaveTheseFiles.isNotEmpty()) {
            val searchItemsQuery = BooleanQuery()
            for(file in search.itemsMustHaveTheseFiles.filterNotNull().filter { it.id != null }) {
                searchItemsQuery.add(TermQuery(Term(FieldName.ItemAttachedFilesIds, file.id)), BooleanClause.Occur.MUST)
            }

            query.add(searchItemsQuery, BooleanClause.Occur.MUST)
        }
    }

    private fun addQueryForSearchTerm(termsToFilterFor: List<SearchTerm>, query: BooleanQuery, search: ItemsSearch) {
        if(termsToFilterFor.isEmpty()) {
            query.add(WildcardQuery(Term(FieldName.ItemId, "*")), BooleanClause.Occur.MUST)
        }
        else {
            for(term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term.term)
                val wildcardEscapedTerm = "*$escapedTerm*"

                val termQuery = BooleanQuery()
                val booleanClause = if (term.match == SearchTermMatch.Contains) BooleanClause.Occur.SHOULD else BooleanClause.Occur.MUST_NOT

                if (term.match == SearchTermMatch.ContainsNot) { // needed as otherwise Occur.MUST_NOT would return no documents / search results at all
                    termQuery.add(MatchAllDocsQuery(), BooleanClause.Occur.MUST)
                }

                if(search.searchInContent) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemContent, escapedTerm)), booleanClause)
                }
                if(search.searchInSummary) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemSummary, escapedTerm)), booleanClause)
                }
                if(search.searchInSource) {
                    termQuery.add(WildcardQuery(Term(FieldName.ItemIndication, wildcardEscapedTerm)), booleanClause)
                    termQuery.add(PrefixQuery(Term(FieldName.ItemSeries, escapedTerm)), booleanClause)
                    termQuery.add(WildcardQuery(Term(FieldName.ItemSourcePublishingDateString, wildcardEscapedTerm)), booleanClause)
                    termQuery.add(PrefixQuery(Term(FieldName.ItemSource, escapedTerm)), booleanClause)
                }
                if(search.searchInTags) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemTagsNames, escapedTerm)), booleanClause)
                }
                if(search.searchInFiles) {
                    termQuery.add(PrefixQuery(Term(FieldName.ItemAttachedFilesDetails, escapedTerm)), booleanClause)
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