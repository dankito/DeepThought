package net.dankito.service.search

import net.dankito.data_access.database.ChangedEntity
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.*
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.specific.*
import net.dankito.service.search.writerandsearcher.*
import net.dankito.synchronization.model.BaseEntity
import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.util.AsyncProducerConsumerQueue
import net.dankito.util.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.services.Times
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class LuceneSearchEngine(private val dataManager: DataManager, private val languageDetector: ILanguageDetector, osHelper: OsHelper, threadPool: IThreadPool, private val eventBus: IEventBus,
                         itemService: ItemService, tagService: TagService, sourceService: SourceService, seriesService: SeriesService,
                         readLaterArticleService: ReadLaterArticleService, fileService: FileService, localFileInfoService: LocalFileInfoService)
    : SearchEngineBase(threadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(LuceneSearchEngine::class.java)
    }


    private val itemIdsIndexWriterAndSearcher = ItemIdsIndexWriterAndSearcher(itemService, eventBus, osHelper, threadPool)

    private val itemIndexWriterAndSearcher = ItemIndexWriterAndSearcher(itemService, eventBus, osHelper, threadPool)

    private val tagIndexWriterAndSearcher = TagIndexWriterAndSearcher(tagService, eventBus, osHelper, threadPool, itemIndexWriterAndSearcher)

    private val sourceIndexWriterAndSearcher = SourceIndexWriterAndSearcher(sourceService, eventBus, osHelper, threadPool)

    private val seriesIndexWriterAndSearcher = SeriesIndexWriterAndSearcher(seriesService, eventBus, osHelper, threadPool)

    private val readLaterArticleIndexWriterAndSearcher = ReadLaterArticleIndexWriterAndSearcher(readLaterArticleService, eventBus, osHelper, threadPool)

    private val fileIndexWriterAndSearcher = FileLinkIndexWriterAndSearcher(fileService, eventBus, osHelper, threadPool)

    private val localFileInfoIndexWriterAndSearcher = LocalFileInfoIndexWriterAndSearcher(localFileInfoService, eventBus, osHelper, threadPool)

    private val indexWritersAndSearchers: List<IndexWriterAndSearcher<*>>

    private lateinit var defaultIndexDirectory: Directory


    init {
        indexWritersAndSearchers = listOf(itemIdsIndexWriterAndSearcher, itemIndexWriterAndSearcher, tagIndexWriterAndSearcher, sourceIndexWriterAndSearcher,
                seriesIndexWriterAndSearcher, readLaterArticleIndexWriterAndSearcher, fileIndexWriterAndSearcher, localFileInfoIndexWriterAndSearcher)

        createDirectoryAndIndexSearcherAndWritersAsync()
    }


    private fun createDirectoryAndIndexSearcherAndWritersAsync() {
        thread(priority = Thread.MAX_PRIORITY) { initializeDirectoriesAndIndexSearcherAndWriters() }
    }

    private fun initializeDirectoriesAndIndexSearcherAndWriters() {
        try {
            val indexBaseDir = File(dataManager.dataFolderPath, "index")
            val indexDirExists = indexBaseDir.exists()

            val defaultIndexDirectoryFile = File(indexBaseDir, "default")
            defaultIndexDirectory = FSDirectory.open(defaultIndexDirectoryFile)

            for(writerAndSearcher in indexWritersAndSearchers) {
                writerAndSearcher.createDirectory(indexBaseDir)
            }

            initializeIndexSearchersAndWriters()

            dataManager.addInitializationListener {
                if(indexDirExists == false) {
                    // TODO: inform user that index is going to be rebuilt and that this takes some time
                    rebuildIndex() // do not rebuild index asynchronously as Application depends on some functions of SearchEngine (like Items without Tags)
                }

                searchEngineInitialized()
            }
        } catch (ex: Exception) {
            log.error("Could not open Lucene Index Directory" , ex)
        }
    }

    private fun initializeIndexSearchersAndWriters() {
        val defaultAnalyzer = LanguageDependentAnalyzer(languageDetector)

        for(writerAndSearcher in indexWritersAndSearchers) {
            writerAndSearcher.initialize(defaultAnalyzer)
        }
    }

    override fun searchEngineInitialized() {
        super.searchEngineInitialized()

        Timer().schedule(Times.DefaultDelayBeforeUpdatingIndexSeconds * 1000L) {
            updateIndex()
            optimizeIndicesIfNeeded()
        }
    }

    /**
     * There are many reasons why the index isn't in sync anymore with database (e.g. an entity got synchronized but app gets closed before it gets indexed).
     * This methods (re-)indexes all entities with changes since dataManager.localSettings.lastSearchIndexUpdateSequenceNumber to ensure index is up to date again
     */
    private fun updateIndex() {
        log.info("Starting updating index with last index update sequence number ${dataManager.localSettings.lastSearchIndexUpdateSequenceNumber}")

        val updatedEntityTypes = HashSet<Class<BaseEntity>>()
        val queue = AsyncProducerConsumerQueue<ChangedEntity<BaseEntity>>(2) {
            updatedEntityTypes.add(it.entityClass)
            updateEntityInIndex(it)
        }

        val currentSequenceNumber = dataManager.entityManager.getAllEntitiesUpdatedAfter<BaseEntity>(dataManager.localSettings.lastSearchIndexUpdateSequenceNumber, queue)

        while(queue.isEmpty == false) { try { Thread.sleep(50) } catch(ignored: Exception) { } } // wait till queue is empty otherwise not all entity types would get added to updatedEntityTypes

        dataManager.localSettings.lastSearchIndexUpdateSequenceNumber = currentSequenceNumber
        dataManager.localSettingsUpdated()

        // TODO: why Local? as most probably it's been due to a remote change when an entity couldn't get indexed. I see that sending Synchronization is also dangerous,
        // e.g. currently editing an entity which got updated in index -> an alert gets shown. Introduce an extra value for it? ProbablySynchronization? EnsuringEntityIsUpToDate?
        updatedEntityTypes.forEach { eventBus.postAsync(EntitiesOfTypeChanged(it, EntityChangeType.Updated, EntityChangeSource.Local)) }

        log.info("Done updating index")
    }

    private fun updateEntityInIndex(changedEntity: ChangedEntity<BaseEntity>) {
        when(changedEntity.entityClass) {
            Item::class.java -> {
                itemIdsIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<Item>)
                itemIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<Item>)
            }
            Tag::class.java -> tagIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<Tag>)
            Series::class.java -> seriesIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<Series>)
            Source::class.java -> sourceIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<Source>)
            ReadLaterArticleService::class.java -> readLaterArticleIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<ReadLaterArticle>)
            FileLink::class.java -> fileIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<FileLink>)
            LocalFileInfo::class.java -> localFileInfoIndexWriterAndSearcher.updateEntityInIndex(changedEntity as ChangedEntity<LocalFileInfo>)
        }
    }

    /**
     * Checks if time since last optimization run is greater than Times.DefaultIntervalToRunIndexOptimizationDays and if so calls optimizeIndices()
     */
    private fun optimizeIndicesIfNeeded() {
        val startTime = Date()
        val timeSinceLastOptimizationMillis = startTime.time - dataManager.localSettings.lastSearchIndexOptimizationTime.time
        if(timeSinceLastOptimizationMillis > Times.DefaultIntervalToRunIndexOptimizationDays * 24 * 60 * 60 * 1000) {
            optimizeIndices()

            dataManager.localSettings.lastSearchIndexOptimizationTime = startTime
            dataManager.localSettingsUpdated()
        }
    }

    /**
     * From time to time index needs an optimization, remove deleted documents from index and merge index files into a single file.
     */
    private fun optimizeIndices() {
        log.info("Starting to optimize indices ...")

        itemIdsIndexWriterAndSearcher.optimizeIndex()
        itemIndexWriterAndSearcher.optimizeIndex()
        tagIndexWriterAndSearcher.optimizeIndex()
        seriesIndexWriterAndSearcher.optimizeIndex()
        sourceIndexWriterAndSearcher.optimizeIndex()
        readLaterArticleIndexWriterAndSearcher.optimizeIndex()
        fileIndexWriterAndSearcher.optimizeIndex()
        localFileInfoIndexWriterAndSearcher.optimizeIndex()

        log.info("Done optimizing indices")
    }


    override fun close() {
        for(writerAndSearcher in indexWritersAndSearchers) {
            writerAndSearcher.close()
        }
    }


    private fun rebuildIndex() {
        deleteIndex()

        log.info("Going to rebuild Lucene index ...")

        indexWritersAndSearchers.forEach { it.indexAllEntities() }

        log.info("Done rebuilding Lucene Index.");
    }

    /**
     *
     *
     * Deletes complete Lucene index.
     * We hope you know what you are doing.
     *
     */
    private fun deleteIndex() {
        log.info("Going to delete Lucene Index ...")

        try {
            for(writerAndSearcher in indexWritersAndSearchers) {
                writerAndSearcher.deleteIndex()
            }

            log.info("Lucene Index successfully deleted")
        } catch (ex: Exception) {
            log.error("Could not delete Lucene index", ex)
        }
    }


    /*      ISearchEngine implementation        */

    override fun searchItems(search: ItemsSearch, termsToSearchFor: List<String>) {
        if(search.isSearchingForItemIds()) {
            itemIdsIndexWriterAndSearcher.searchItemIds(search, termsToSearchFor)
        }
        else {
            itemIndexWriterAndSearcher.searchItems(search, termsToSearchFor)
        }
    }

    override fun searchTags(search: TagsSearch, termsToSearchFor: List<String>) {
        tagIndexWriterAndSearcher.searchTags(search, termsToSearchFor)
    }

    override fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>) {
        tagIndexWriterAndSearcher.searchFilteredTags(search, termsToSearchFor)
    }

    override fun searchSources(search: SourceSearch, termsToSearchFor: List<String>) {
        sourceIndexWriterAndSearcher.searchSources(search, termsToSearchFor)
    }

    override fun searchSeries(search: SeriesSearch, termsToSearchFor: List<String>) {
        seriesIndexWriterAndSearcher.searchSeries(search, termsToSearchFor)
    }

    override fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        readLaterArticleIndexWriterAndSearcher.searchReadLaterArticles(search, termsToSearchFor)
    }

    override fun searchFiles(search: FilesSearch, termsToSearchFor: List<String>) {
        fileIndexWriterAndSearcher.searchFiles(search, termsToSearchFor)
    }

    override fun searchLocalFileInfo(search: LocalFileInfoSearch) {
        localFileInfoIndexWriterAndSearcher.searchLocalFileInfo(search)
    }

}