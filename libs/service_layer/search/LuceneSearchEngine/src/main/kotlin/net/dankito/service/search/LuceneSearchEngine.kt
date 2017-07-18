package net.dankito.service.search

import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.specific.*
import net.dankito.service.search.writerandsearcher.*
import net.dankito.utils.IThreadPool
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.concurrent.thread


class LuceneSearchEngine(private val dataManager: DataManager, threadPool: IThreadPool, eventBus: IEventBus, entryService: EntryService, tagService: TagService,
                         referenceService: ReferenceService, readLaterArticleService: ReadLaterArticleService)
    : SearchEngineBase(threadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(LuceneSearchEngine::class.java)
    }


    private val entryIndexWriterAndSearcher = EntryIndexWriterAndSearcher(entryService, eventBus)

    private val tagIndexWriterAndSearcher = TagIndexWriterAndSearcher(tagService, eventBus, entryIndexWriterAndSearcher)

    private val referenceIndexWriterAndSearcher = ReferenceIndexWriterAndSearcher(referenceService, eventBus)

    private val readLaterArticleIndexWriterAndSearcher = ReadLaterArticleIndexWriterAndSearcher(readLaterArticleService, eventBus)

    private val indexWritersAndSearchers: List<IndexWriterAndSearcher<*>>

    private lateinit var defaultIndexDirectory: Directory

    private lateinit var defaultAnalyzer: Analyzer

    private var isIndexReady = false


    init {
        indexWritersAndSearchers = listOf(entryIndexWriterAndSearcher, tagIndexWriterAndSearcher, referenceIndexWriterAndSearcher, readLaterArticleIndexWriterAndSearcher)

        createDirectoryAndIndexSearcherAndWriterForDeepThoughtAsync()
    }


    private fun createDirectoryAndIndexSearcherAndWriterForDeepThoughtAsync() {
        thread(priority = Thread.MAX_PRIORITY) { createDirectoryAndIndexSearcherAndWriterForDeepThought() }
    }

    private fun createDirectoryAndIndexSearcherAndWriterForDeepThought() {
        try {
            val indexBaseDir = File(dataManager.dataFolderPath, "index")
            val indexDirExists = indexBaseDir.exists()

            val defaultIndexDirectoryFile = File(indexBaseDir, "default")
            defaultIndexDirectory = FSDirectory.open(defaultIndexDirectoryFile)

            for(writerAndSearcher in indexWritersAndSearchers) {
                writerAndSearcher.createDirectory(indexBaseDir)
            }

            isIndexReady = true

            createIndexSearchersAndWriters()

            if (indexDirExists == false) {
                // TODO: inform user that index is going to be rebuilt and that this takes some time
                rebuildIndex() // do not rebuild index asynchronously as Application depends on some functions of SearchEngine (like Entries without Tags)
            }

            searchEngineInitialized()
        } catch (ex: Exception) {
            log.error("Could not open Lucene Index Directory" , ex)
        }
    }

    private fun createIndexSearchersAndWriters() {
//        defaultAnalyzer = DeepThoughtAnalyzer()
        defaultAnalyzer = StandardAnalyzer(Version.LUCENE_47)

        for(writerAndSearcher in indexWritersAndSearchers) {
            writerAndSearcher.initialize(defaultAnalyzer)
        }
    }


    override fun close() {
        for(writerAndSearcher in indexWritersAndSearchers) {
            writerAndSearcher.close()
        }
    }


    private fun rebuildIndex() {
        if (isIndexReady == false) {
            return
        }

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

    override fun searchEntries(search: EntriesSearch, termsToSearchFor: List<String>) {
        entryIndexWriterAndSearcher.searchEntries(search, termsToSearchFor)
    }

    override fun searchTags(search: TagsSearch, termsToSearchFor: List<String>) {
        tagIndexWriterAndSearcher.searchTags(search, termsToSearchFor)
    }

    override fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>) {
        tagIndexWriterAndSearcher.searchFilteredTags(search, termsToSearchFor)
    }

    override fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>) {
        referenceIndexWriterAndSearcher.searchReferences(search, termsToSearchFor)
    }

    override fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        readLaterArticleIndexWriterAndSearcher.searchReadLaterArticles(search, termsToSearchFor)
    }

}