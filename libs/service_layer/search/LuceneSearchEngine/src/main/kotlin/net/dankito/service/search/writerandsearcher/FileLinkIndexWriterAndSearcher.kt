package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.service.data.FileService
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.document.*
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery


class FileLinkIndexWriterAndSearcher(fileService: FileService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<FileLink>(fileService, eventBus, osHelper, threadPool) {


    override fun getDirectoryName(): String {
        return "files"
    }

    override fun getIdFieldName(): String {
        return FieldName.FileId
    }


    override fun addEntityFieldsToDocument(entity: FileLink, doc: Document) {
        // for an not analyzed String it's important to index it lower case as only then lower case search finds it
        doc.add(StringField(FieldName.FileName, entity.name.toLowerCase(), Field.Store.NO))
        doc.add(StringField(FieldName.FileUri, entity.uriString.toLowerCase(), Field.Store.NO))
        addBooleanFieldToDocument(FieldName.FileIsLocalFile, entity.isLocalFile, doc)

        // TODO: file type
        doc.add(LongField(FieldName.FileFileSize, entity.fileSize, Field.Store.YES))
        entity.fileLastModified?.let { doc.add(LongField(FieldName.FileFileLastModified, it.time, Field.Store.YES)) }

        doc.add(Field(FieldName.FileDescription, entity.description, TextField.TYPE_NOT_STORED))
        doc.add(StringField(FieldName.FileSourceUri, entity.sourceUriString.toLowerCase(), Field.Store.NO))

        entity.localFileInfo?.let { doc.add(StringField(FieldName.FileLocalFileInfoId, it.id, Field.Store.YES)) }
    }


    fun getLocalFileInfo(file: FileLink): LocalFileInfo? {
        if(file.id == null) { // file not persisted and therefore indexed yet
            return null
        }

        val query = TermQuery(Term(getIdFieldName(), file.id))

        val queryResultPair = executeQuery(query, 1)

        queryResultPair?.second?.let { scoreDocs ->
            if(scoreDocs.size > 0) {
                val searcher = queryResultPair.first
                val doc = searcher.doc(scoreDocs[0].doc)

                doc.getField(FieldName.FileLocalFileInfoId)?.let { field -> // field is null when LocalFileInfo hasn't been stored yet
                    val id = field.stringValue()

                    return entityService.entityManager.getEntityById(LocalFileInfo::class.java, id)
                }
            }
        }

        return null
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