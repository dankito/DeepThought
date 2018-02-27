package net.dankito.service.search.writerandsearcher

import net.dankito.synchronization.database.ChangedEntity
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.*
import net.dankito.service.search.results.LazyLoadingLuceneSearchResultsList
import net.dankito.service.search.writerandsearcher.sorting.CorrectStringComparatorSource
import net.dankito.synchronization.model.BaseEntity
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Synchronized
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


abstract class IndexWriterAndSearcher<TEntity : BaseEntity>(val entityService: EntityServiceBase<TEntity>, eventBus: IEventBus, protected val osHelper: OsHelper, protected val threadPool: IThreadPool) {

    companion object {
        protected const val DEFAULT_COUNT_MAX_SEARCH_RESULTS = 100000

        private const val InstanceLock = "LOCK"

        private val log = LoggerFactory.getLogger(IndexWriterAndSearcher::class.java)
    }


    var isReadOnly = false

    protected lateinit var defaultAnalyzer: LanguageDependentAnalyzer

    private var indexDirectory: File? = null

    private var directory: Directory? = null

    private var writer: IndexWriter? = null

    private var directoryReader: DirectoryReader? = null

    private var indexSearcher: IndexSearcher? = null

    private var commitIndicesTimer: Timer? = null

    private var eventBusListener: Any


    init {
        eventBusListener = createEntityChangedListener() // we need to hold on to a reference otherwise MBassador's WeakReference would be garbage collected

        eventBus.register(eventBusListener)
    }

    abstract fun createEntityChangedListener(): Any


    /**
     * Call this method on app start.
     * On app start we don't want to create an IndexWriter as this consumes a lot of time.
     * What we want on app start is a quick start-up, so on first access only create an IndexSearcher on a readOnly Directory.
     * Later on, when IndexWriter is needed for first write operation, we re-create Directory and IndexSearcher
     */
    fun initialize(defaultAnalyzer: LanguageDependentAnalyzer) {
        this.defaultAnalyzer = defaultAnalyzer
    }


    fun createDirectory(indexBaseDir: File) : Directory? {
        val indexDirectory = File(indexBaseDir, getDirectoryName())
        indexDirectory.mkdirs() // necessary?

        directory = FSDirectory.open(indexDirectory)

        this.indexDirectory = indexDirectory

        return directory
    }

    abstract fun getDirectoryName(): String


    /**
     *
     *
     * Creates a new IndexWriter with specified Analyzer.
     * *
     * @return Created IndexWriter or null on failure!
     */
    @Throws(Exception::class)
    private fun createIndexWriter(defaultAnalyzer: Analyzer): IndexWriter? {
        try {
            val config = IndexWriterConfig(Version.LUCENE_47, defaultAnalyzer)
            config.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND

            this.writer = IndexWriter(directory, config)
        } catch(e: Exception) {
//            if(e is LockObtainFailedException) { // TODO: really only on LockObtainFailedException?
                isReadOnly = true
//            }

            log.error("Could not create IndexWriter for $this, index is now marked as readOnly", e)
            throw e
        }

        return writer
    }

    private fun getWriter(): IndexWriter? {
        synchronized(InstanceLock) {
            if(writer == null) {
                writer = createIndexWriter(defaultAnalyzer)
            }

            return writer
        }
    }

    /**
     *
     *
     * On opening an index directory there are no new changes yet
     * so on first call call this simple method to create an IndexSearcher.
     *
     * @return
     */
    private fun createIndexSearcher(isReadOnly: Boolean) {
        directoryReader = createDirectoryReader(isReadOnly)

        indexSearcher = IndexSearcher(directoryReader)
    }

    private fun getIndexSearcher(): IndexSearcher? {
        synchronized(InstanceLock) {
            if(indexSearcher == null) {
                try {
                    createIndexSearcher(isReadOnly)
                } catch (e: Exception) {
                    log.error("Could not create IndexSearcher for $this", e)
                }

            }

            return indexSearcher
        }
    }

    private fun createDirectoryReader(isReadOnly: Boolean): DirectoryReader? {
        if(isReadOnly || writer == null) { // on app start writer is null as opening an index read only speeds up app start. Later on, when writer is needed, we re-create directory and indexSearcher
            return DirectoryReader.open(directory) // open readonly
        }
        else if(directoryReader == null) {
            return DirectoryReader.open(writer, true)
        }
        else { // index has changed
            val newDirectoryReader = DirectoryReader.openIfChanged(directoryReader, writer, true)

            if(newDirectoryReader != null) {
                return newDirectoryReader
            }
            else {
                return directoryReader
            }
        }
    }


    fun close() {
        indexSearcher = null

        writer?.let { writer ->
            writer.commit()
            writer.close()
        }
        writer = null

        directoryReader?.close()
        directoryReader = null

        directory?.close()
        directory = null
    }


    fun handleEntityChange(entityChanged: EntityChanged<TEntity>) {
        if(entityChanged.changeType == EntityChangeType.Created) {
            indexEntity(entityChanged.entity)
        }
        else if(entityChanged.changeType == EntityChangeType.Updated && entityChanged.entity.id != null) { // an entity got updated and shortly after deleted, e.g. a file that has been removed from an item (EntityChangedNotifier fires delayed)
            updateEntityInIndex(entityChanged.entity)
        }
        else if(entityChanged.changeType == EntityChangeType.PreDelete || (entityChanged.changeType == EntityChangeType.Deleted && entityChanged.entity.id != null)) {
            removeEntityFromIndex(entityChanged.entity)
        }
    }


    fun updateIndex() {
        log.info("Updating index of $this ...")

        try {
            val indexedEntities = getIndexedEntities()

            for(entity in entityService.getAll()) {
                handleIfEntityShouldBeUpdated(indexedEntities, entity)
            }

            writer?.let { writer ->
                indexedEntities.forEach { removeEntityFromIndex(writer, it.key) }
            }
        } catch(e: Exception) { log.error("Could not update index for ${javaClass.simpleName}", e) }
    }

    private fun handleIfEntityShouldBeUpdated(indexedEntities: HashMap<String, Long?>, entity: TEntity) {
        val modifiedOn = indexedEntities.remove(entity.id)

        if(modifiedOn == null) {
            indexEntity(entity)
        }
        else if(modifiedOn != entity.modifiedOn.time) {
            updateEntityInIndex(entity)
        }
    }

    private fun getIndexedEntities(): HashMap<String, Long?> {
        val indexedEntities = HashMap<String, Long?>()

        executeQuery(WildcardQuery(Term(getIdFieldName(), "*")))?.let { (searcher, hits) ->
            hits.forEach {
                val hitDoc = searcher.doc(it.doc)
                val id = hitDoc.getField(getIdFieldName()).stringValue()
                val modifiedOn = hitDoc.getField(FieldName.ModifiedOn)?.numericValue()?.toLong()

                indexedEntities.put(id, modifiedOn)
            }
        }

        return indexedEntities
    }

    fun indexAllEntities() {
        try {
            for(entity in entityService.getAll()) {
                indexEntity(entity)
            }
        } catch(e: Exception) { log.error("Could not index all entities for ${javaClass.simpleName}", e) }
    }

    fun indexEntity(entity: TEntity) {
        try {
            val doc = Document()

            doc.add(StringField(getIdFieldName(), entity.id, Field.Store.YES)) // id and modifiedOn are added to all documents
            doc.add(LongField(FieldName.ModifiedOn, entity.modifiedOn.time, Field.Store.YES))

            addEntityFieldsToDocument(entity, doc)

            indexDocument(doc)
        } catch (e: Exception) {
            log.error("Could not index Entity " + entity, e)
        }
    }

    abstract fun addEntityFieldsToDocument(entity: TEntity, doc: Document)

    protected fun addBooleanFieldToDocument(fieldName: String, fieldValue: Boolean, doc: Document) {
        val convertedFieldValue = if(fieldValue) FieldValue.BooleanFieldTrueValue else FieldValue.BooleanFieldFalseValue

        doc.add(StringField(fieldName, convertedFieldValue, Field.Store.NO))
    }

    private fun indexDocument(doc: Document) {
        try {
            getWriter()?.let { writer ->
                writer.addDocument(doc)

                commitChangesToWriter()
            }
        } catch (e: Exception) {
            log.error("Could not index Document " + doc, e)
        }
    }


    fun updateEntityInIndex(changedEntity: ChangedEntity<TEntity>) {
        if(changedEntity.isDeleted) {
            changedEntity.id?.let { removeEntityFromIndex(it) }
        }
        else if(changedEntity.entity != null) {
            changedEntity.entity?.let { updateEntityInIndex(it) }
        }
    }

    private fun updateEntityInIndex(entity: TEntity) {
        removeEntityFromIndex(entity)
        indexEntity(entity)
    }

    private fun removeEntityFromIndex(removedEntity: TEntity) {
        removedEntity.id?.let { removeEntityFromIndex(it) }
    }

    private fun removeEntityFromIndex(entityId: String) {
        if(isReadOnly) {
            return
        }

        try {
            getWriter()?.let { writer ->
                removeEntityFromIndex(writer, entityId)
            }
        } catch (e: Exception) {
            log.error("Could not delete Document for removed entity with id " + entityId, e)
        }
    }

    private fun removeEntityFromIndex(writer: IndexWriter, entityId: String) {
        writer.deleteDocuments(Term(getIdFieldName(), entityId))

        commitChangesToWriter()
    }

    abstract fun getIdFieldName(): String


    fun deleteIndex() {
        synchronized(InstanceLock) {
            log.info("Deleting index for $this")
            writer?.close()
            writer = null

            indexDirectory?.let { dir ->
                dir.listFiles().forEach { it.delete() }
            }
        }
    }


    fun optimizeIndex() {
        getWriter()?.let { writer ->
            writer.forceMergeDeletes()
            writer.forceMerge(1)

            writer.commit()

            log.info("Optimized index of $this")
        }
    }


    /**
     * Calling commit() is a costly operation
     * -> don't call it on each update / deletion, wait some time before commit accumulated changes.
     */
    @Synchronized private fun commitChangesToWriter() {
        getWriter()?.commit()

        markIndexHasBeenUpdated() // so that on next search updates are reflected
    }


    private fun markIndexHasBeenUpdated() {
        indexSearcher = null
    }


    internal fun executeQuery(query: Query, countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption): Pair<IndexSearcher, Array<ScoreDoc>>? {
        log.info("Executing Query " + query)

        getIndexSearcher()?.let { searcher ->
            val hits = searcher.search(query, countMaxSearchResults, getSorting(sortOptions.asList())).scoreDocs

            return Pair(searcher, hits)
        }

        return null
    }

    protected fun executeQuery(query: Query, resultEntityClass: Class<TEntity>, countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption): List<TEntity> {
        executeQuery(query, countMaxSearchResults, *sortOptions)?.let { (searcher, hits) ->
            return LazyLoadingLuceneSearchResultsList<TEntity>(entityService.entityManager, searcher, resultEntityClass, getIdFieldName(), hits, osHelper, threadPool)
        }

        return listOf()
    }

    private fun getSorting(sortOptions: List<SortOption>): Sort {
        val sort = Sort()

        if(sortOptions.isNotEmpty()) {
            val sortFields = arrayOfNulls<SortField>(sortOptions.size)

            for(i in sortOptions.indices) {
                val (fieldName, order, type) = sortOptions[i]

                if(type == SortField.Type.STRING || type == SortField.Type.STRING_VAL) { // TODO: only use one CorrectStringComparatorSource() instance?
                    sortFields[i] = SortField(fieldName, CorrectStringComparatorSource(), order === SortOrder.Descending)
                }
                else {
                    sortFields[i] = SortField(fieldName, type, order === SortOrder.Descending)
                }
            }

            sort.setSort(*sortFields)
        }

        return sort
    }

    protected fun executeQueryForSearchWithCollectionResult(search: SearchWithCollectionResult<TEntity>, query: Query, resultEntityClass: Class<TEntity>,
                                                            countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption) {
        if(search.isInterrupted)
            return

        try {
            getIndexSearcher()?.let {
                search.results = executeQuery(query, resultEntityClass, countMaxSearchResults, *sortOptions)
            }
        } catch (e: Exception) {
            log.error("Could not execute Query " + query.toString(), e)
            search.errorOccurred(e)
        }

        search.fireSearchCompleted()
    }

    protected fun getIdsFromHits(searcher: IndexSearcher, hits: Array<ScoreDoc>): MutableList<String> {
        return hits.map {
            val hitDoc = searcher.doc(it.doc)
            hitDoc.getField(getIdFieldName()).stringValue()
        }.toMutableList()
    }

}