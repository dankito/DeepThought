package net.dankito.service.search

import net.dankito.deepthought.model.Entry
import net.dankito.service.data.EntryService
import net.dankito.service.data.messages.EntryChanged
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*


class EntryIndexWriterAndSearcher(entryService: EntryService) : IndexWriterAndSearcher<Entry>(entryService) {

    override fun getDirectoryName(): String {
        return "entries"
    }


    override fun createDocumentFromEntry(entry: Entry): Document {
        val doc = Document()

        doc.add(StringField(FieldName.EntryId, entry.id, Field.Store.YES))

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

    override fun getIdFieldName(): String {
        return FieldName.EntryId
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler
            fun entityChanged(entryChanged: EntryChanged) {
                handleEntityChange(entryChanged)
            }

        }
    }

}