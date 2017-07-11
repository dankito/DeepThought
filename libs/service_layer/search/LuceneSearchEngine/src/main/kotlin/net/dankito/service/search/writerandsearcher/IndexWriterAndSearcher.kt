package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.SearchWithCollectionResult
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.results.LazyLoadingLuceneSearchResultsList
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.LockObtainFailedException
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


abstract class IndexWriterAndSearcher<TEntity : BaseEntity>(val entityService: EntityServiceBase<TEntity>, eventBus: IEventBus) {

    companion object {
        protected const val DEFAULT_COUNT_MAX_SEARCH_RESULTS = 1000

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

        eventBus.register(eventBusListener)
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

    protected fun getIndexSearcher(): IndexSearcher? {
        if (indexSearcher == null) {
            try {
                createIndexSearcher(isReadOnly)
            } catch (e: Exception) {
                log.error("Could not create IndexSearcher for $this", e)
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
        else if(entityChanged.changeType == EntityChangeType.PreDelete || (entityChanged.changeType == EntityChangeType.Deleted && entityChanged.entity.id != null)) {
            removeEntityFromIndex(entityChanged.entity)
        }
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
            val doc = createDocumentForEntity(entity)

            indexDocument(doc)
        } catch (e: Exception) {
            log.error("Could not index Entity " + entity, e)
        }
    }

    abstract fun createDocumentForEntity(entity: TEntity): Document

    protected fun indexDocument(doc: Document) {
        try {
            writer?.let { writer ->
                log.info("Indexing document {}", doc)
                writer.addDocument(doc)

                startCommitIndicesTimer()

                markIndexHasBeenUpdated() // so that on next search updates are reflected
            }
        } catch (e: Exception) {
            log.error("Could not index Document " + doc, e)
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
        } catch (e: Exception) {
            log.error("Could not delete Document for removed entity " + removedEntity, e)
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


    protected fun executeQuery(query: Query, countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption): Pair<IndexSearcher, Array<ScoreDoc>>? {
        log.debug("Executing Query " + query)

        getIndexSearcher()?.let { searcher ->
            val hits = searcher.search(query, countMaxSearchResults, getSorting(sortOptions.asList())).scoreDocs

            return Pair(searcher, hits)
        }

        return null
    }

    protected fun executeQuery(query: Query, resultEntityClass: Class<TEntity>, countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption): List<TEntity> {
        executeQuery(query, countMaxSearchResults, *sortOptions)?.let { (searcher, hits) ->
            return LazyLoadingLuceneSearchResultsList<TEntity>(entityService.entityManager, searcher, resultEntityClass, getIdFieldName(), hits)
        }

        return listOf()
    }

    protected fun getSorting(sortOptions: List<SortOption>): Sort {
        val sort = Sort()

        if (sortOptions.isNotEmpty()) {
            val sortFields = arrayOfNulls<SortField>(sortOptions.size)

            for(i in sortOptions.indices) {
                val (fieldName, order, type) = sortOptions[i]

                sortFields[i] = SortField(fieldName, type, order === SortOrder.Descending)
            }

            sort.setSort(*sortFields)
        }

        return sort
    }

    protected fun executeQueryForSearchWithCollectionResult(search: SearchWithCollectionResult<TEntity>, query: Query, resultEntityClass: Class<TEntity>,
                                                            countMaxSearchResults: Int = DEFAULT_COUNT_MAX_SEARCH_RESULTS, vararg sortOptions: SortOption) {
        if (search.isInterrupted)
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

}