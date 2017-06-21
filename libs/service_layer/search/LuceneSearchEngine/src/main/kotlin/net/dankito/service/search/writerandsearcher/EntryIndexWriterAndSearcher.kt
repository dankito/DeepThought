package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Entry
import net.dankito.service.data.EntryService
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.search.FieldName
import net.dankito.service.search.FieldValue
import net.dankito.service.search.FieldValue.NoTagsFieldValue
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.EntriesSearch
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*


class EntryIndexWriterAndSearcher(entryService: EntryService) : IndexWriterAndSearcher<Entry>(entryService) {

    override fun getDirectoryName(): String {
        return "entries"
    }

    override fun getIdFieldName(): String {
        return FieldName.EntryId
    }


    override fun createDocumentFromEntry(entry: Entry): Document {
        val doc = Document()

        doc.add(StringField(getIdFieldName(), entry.id, Field.Store.YES))

        // TODO: get plain text
//        doc.add(Field(FieldName.EntryAbstract, entry.getAbstractAsPlainText(), TextField.TYPE_NOT_STORED))
//        doc.add(Field(FieldName.EntryContent, entry.getContentAsPlainText(), TextField.TYPE_NOT_STORED))
        doc.add(Field(FieldName.EntryAbstract, entry.abstractString, TextField.TYPE_NOT_STORED))
        doc.add(Field(FieldName.EntryContent, entry.content, TextField.TYPE_NOT_STORED))

        doc.add(LongField(FieldName.EntryIndex, entry.entryIndex, Field.Store.YES))

        doc.add(LongField(FieldName.EntryCreated, entry.createdOn.getTime(), Field.Store.YES))
        doc.add(LongField(FieldName.EntryModified, entry.modifiedOn.getTime(), Field.Store.YES))

        if (entry.hasTags()) {
            for (tag in entry.tags) {
                doc.add(StringField(FieldName.EntryTagsIds, tag.id, Field.Store.YES))
                //        doc.add(new StringField(FieldName.EntryTags, tag.getName().toLowerCase(), Field.Store.YES));
            }
        } else
            doc.add(StringField(FieldName.EntryNoTags, FieldValue.NoTagsFieldValue, Field.Store.NO))

        return doc
    }


    fun searchEntries(search: EntriesSearch, termsToFilterFor: Array<String>) {
        val query = BooleanQuery()

        addQueryForOptions(search, query)

        addQueryForSearchTerm(termsToFilterFor, query, search)

        executeQueryForSearchWithCollectionResult(search, query, Entry::class.java, listOf(SortOption(FieldName.EntryCreated, SortOrder.Descending, SortField.Type.LONG)))
    }

    private fun addQueryForOptions(search: EntriesSearch, query: BooleanQuery) {
        if (search.filterOnlyEntriesWithoutTags) {
            query.add(TermQuery(Term(FieldName.EntryNoTags, NoTagsFieldValue)), BooleanClause.Occur.MUST)
        } else if (search.entriesMustHaveTheseTags.size > 0) {
            val filterEntriesQuery = BooleanQuery()
            for (tag in search.entriesMustHaveTheseTags) {
                filterEntriesQuery.add(TermQuery(Term(FieldName.EntryTagsIds, tag.id)), BooleanClause.Occur.MUST)
            }

            query.add(filterEntriesQuery, BooleanClause.Occur.MUST)
        }
    }

    private fun addQueryForSearchTerm(termsToFilterFor: Array<String>, query: BooleanQuery, search: EntriesSearch) {
        if (termsToFilterFor.size == 0) {
            query.add(WildcardQuery(Term(FieldName.EntryId, "*")), BooleanClause.Occur.MUST)
        } else {
            for (term in termsToFilterFor) {
                val escapedTerm = QueryParser.escape(term)
                val termQuery = BooleanQuery()

                if (search.filterContent) {
                    termQuery.add(PrefixQuery(Term(FieldName.EntryContent, escapedTerm)), BooleanClause.Occur.SHOULD)
                }
                if (search.filterAbstract) {
                    termQuery.add(PrefixQuery(Term(FieldName.EntryAbstract, escapedTerm)), BooleanClause.Occur.SHOULD)
                }

                query.add(termQuery, BooleanClause.Occur.MUST)
            }
        }
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = Int.MAX_VALUE)
            fun entityChanged(entryChanged: EntryChanged) {
                handleEntityChange(entryChanged)
            }

        }
    }

}