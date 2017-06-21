package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.search.SearchWithCollectionResult
import net.dankito.service.search.SortOption
import net.dankito.service.search.results.LazyLoadingLuceneSearchResultsList
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.LockObtainFailedException
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


abstract class IndexWriterAndSearcher<TEntity : BaseEntity>(val entityService: EntityServiceBase<TEntity>) {

    companion object {
        private const val WAIT_TIME_BEFORE_COMMITTING_INDICES_MILLIS = 1500L

        private val log = LoggerFactory.getLogger(IndexWriterAndSearcher::class.java)
    }


    var isReadOnly = false

    private var directory: Directory? = null

    private var writer: IndexWriter? = null

    private var directoryReader: DirectoryReader? = null

    private var indexSearcher: IndexSearcher? = null

    private var commitIndicesTimer: Timer? = null

    private var eventBusListener: Any


    init {
        eventBusListener = createEntityChangedListener() // we need to hold on to a reference otherwise MBassador's WeakReference would be garbage collected

        entityService.eventBus.register(eventBusListener)
    }

    abstract fun createEntityChangedListener(): Any


    fun createDirectory(indexBaseDir: File) : Directory? {
        val indexDirectory = File(indexBaseDir, getDirectoryName())

        directory = FSDirectory.open(indexDirectory)

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
    fun createIndexWriter(defaultAnalyzer: Analyzer): IndexWriter? {
        try {
            val config = IndexWriterConfig(Version.LUCENE_47, defaultAnalyzer)
            config.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND

            this.writer = IndexWriter(directory, config)
        } catch(e: Exception) {
            if (e is LockObtainFailedException) {
                isReadOnly = true
            }

            log.error("Could not create IndexWriter for $this", e)
            throw e
        }

        return writer
    }

    /**
     *
     *
     * On opening an index directory there are no new changes yet
     * so on first call call this simple method to create an IndexSearcher.
     *
     * @return
     */
    fun createIndexSearcher(isReadOnly: Boolean) {
        directoryReader = createDirectoryReader(isReadOnly)

        indexSearcher = IndexSearcher(directoryReader)
    }

    private fun getIndexSearcher(): IndexSearcher? {
        if (indexSearcher == null) {
            try {
                createIndexSearcher(isReadOnly)
            } catch (ex: Exception) {
                log.error("Could not create IndexSearcher for $this", ex)
            }

        }

        return indexSearcher
    }

    private fun createDirectoryReader(isReadOnly: Boolean): DirectoryReader? {
        if (isReadOnly) {
            return DirectoryReader.open(directory) // open readonly
        }
        else if (directoryReader == null) { // on startup
            return DirectoryReader.open(writer, true)
        }
        else {
            val newDirectoryReader = DirectoryReader.openIfChanged(directoryReader, writer, true)

            if (newDirectoryReader != null) {
                return newDirectoryReader
            }
            else {
                return directoryReader
            }
        }
    }


    fun handleEntityChange(entityChanged: EntityChanged<TEntity>) {
        if(entityChanged.changeType == EntityChangeType.Created) {
            indexEntity(entityChanged.entity)
        }
        else if(entityChanged.changeType == EntityChangeType.Updated) {
            removeEntityFromIndex(entityChanged.entity)
            indexEntity(entityChanged.entity)
        }
        else if(entityChanged.changeType == EntityChangeType.Deleted) {
            removeEntityFromIndex(entityChanged.entity)
        }
    }


    fun indexAllEntities() {
        for(entity in entityService.getAll()) {
            indexEntity(entity)
        }
    }

    fun indexEntity(entity: TEntity) {
        try {
            val doc = createDocumentFromEntry(entity)

            indexDocument(doc)
        } catch (ex: Exception) {
            log.error("Could not index Entity " + entity, ex)
        }
    }

    abstract fun createDocumentFromEntry(entity: TEntity): Document

    protected fun indexDocument(doc: Document) {
        try {
            writer?.let { writer ->
                log.info("Indexing document {}", doc)
                writer.addDocument(doc)

                startCommitIndicesTimer()

                markIndexHasBeenUpdated() // so that on next search updates are reflected
            }
        } catch (ex: Exception) {
            log.error("Could not index Document " + doc, ex)
        }
    }


    protected fun removeEntityFromIndex(removedEntity: TEntity) {
        if(isReadOnly) {
            return
        }

        try {
            writer?.let { writer ->
                log.info("Removing Entity {} from index", removedEntity)

                writer.deleteDocuments(Term(getIdFieldName(), removedEntity.id))

                startCommitIndicesTimer()

                markIndexHasBeenUpdated() // so that on next search updates are reflected
            }
        } catch (ex: Exception) {
            log.error("Could not delete Document for removed entity " + removedEntity, ex)
        }
    }

    abstract fun getIdFieldName(): String


    fun deleteIndex() {
        writer?.let { writer ->
            writer.deleteAll()
            writer.prepareCommit()
            writer.commit()

            markIndexHasBeenUpdated()
        }
    }


    /**
     * Calling commit() is a costly operation
     * -> don't call it on each update / deletion, wait some time before commit accumulated changes.
     */
    @Synchronized protected fun startCommitIndicesTimer() {
        if (commitIndicesTimer != null) { // timer already started
            return
        }

        commitIndicesTimer = Timer("Commit Indices Timer")

        commitIndicesTimer?.schedule(object : TimerTask() {
            override fun run() {
                commitIndicesTimer = null

                writer?.commit()
            }
        }, WAIT_TIME_BEFORE_COMMITTING_INDICES_MILLIS)
    }


    private fun markIndexHasBeenUpdated() {
        indexSearcher = null
    }


    @Throws(Exception::class)
    protected fun executeQuery(query: Query, resultEntityClass: Class<TEntity>, sortOptions: List<SortOption>): List<TEntity> {
        log.debug("Executing Query " + query)

        getIndexSearcher()?.let {
            return LazyLoadingLuceneSearchResultsList<TEntity>(entityService.entityManager, it, query, resultEntityClass, getIdFieldName(), 1000, sortOptions)
        }

        return listOf()
    }

    protected fun executeQueryForSearchWithCollectionResult(search: SearchWithCollectionResult<TEntity>, query: Query, resultEntityClass: Class<TEntity>, sortOptions: List<SortOption>) {
        if (search.isInterrupted)
            return

        try {
            getIndexSearcher()?.let {
                search.results = executeQuery(query, resultEntityClass, sortOptions)
            }
        } catch (ex: Exception) {
            log.error("Could not execute Query " + query.toString(), ex)
            // TODO: set error flag in Search
        }

        search.fireSearchCompleted()
    }

}