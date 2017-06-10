package net.dankito.service.search

import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.service.data.DataManager
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.File


class LuceneSearchEngine(private val dataManager: DataManager, private val indexWritersAndSearchers: Set<IndexWriterAndSearcher<*>>) {

    companion object {
        private val log = LoggerFactory.getLogger(LuceneSearchEngine::class.java)
    }


    private lateinit var defaultIndexDirectory: Directory

    private lateinit var defaultAnalyzer: Analyzer

    private var isReadOnly = false

    private var isIndexReady = false

    private var isInitialized = false


    init {
        dataManager.addInitializationListener { dataManagerInitialized() }
    }


    private fun dataManagerInitialized() {
        dataManager.currentDeepThought?.let { createDirectoryAndIndexSearcherAndWriterForDeepThought(it) }
    }

    private fun createDirectoryAndIndexSearcherAndWriterForDeepThought(deepThought: DeepThought) {
        try {
            val indexBaseDir = File(File(dataManager.dataFolderPath, "index"), deepThought.id)
            val indexDirExists = indexBaseDir.exists()

            val defaultIndexDirectoryFile = File(indexBaseDir, "default")
            defaultIndexDirectory = FSDirectory.open(defaultIndexDirectoryFile)

            for(writerAndSearcher in indexWritersAndSearchers) {
                writerAndSearcher.createDirectory(indexBaseDir)
            }

            isIndexReady = true

            createIndexSearchersAndWriters()

            if (indexDirExists == false) {
                rebuildIndex() // do not rebuild index asynchronously as Application depends on some functions of SearchEngine (like Entries without Tags)
            }

            isInitialized = true
        } catch (ex: Exception) {
            log.error("Could not open Lucene Index Directory for DeepThought " + deepThought, ex)
        }
    }

    private fun createIndexSearchersAndWriters() {
//        defaultAnalyzer = DeepThoughtAnalyzer()
        defaultAnalyzer = StandardAnalyzer(Version.LUCENE_47)

        for(writerAndSearcher in indexWritersAndSearchers) {
            createIndexWriter(writerAndSearcher, defaultAnalyzer)

            createIndexSearcherOnOpeningDirectory(writerAndSearcher)
        }
    }

    private fun createIndexWriter(writerAndSearcher: IndexWriterAndSearcher<*>, defaultAnalyzer: Analyzer) {
        if (isReadOnly == false) { // it is better that when once isReadOnly has been set to true not to unset it again even though write access would not be possible again
            try { // as otherwise all changes done till index becomes writable again would be lost which could lead to data inconsistency
                writerAndSearcher.createIndexWriter(defaultAnalyzer)
            } catch (e: Exception) {
                if (writerAndSearcher.isReadOnly) {
                    if (isReadOnly == false) { // TODO
//                        Application.notifyUser(Notification(NotificationType.HasOnlyReadOnlyAccessToData)) // TODO: add message
                    }
                    isReadOnly = true
                }

                isIndexReady = false
            }
        }
    }

    /**
     *
     *
     * On opening an index directory there are no new changes yet
     * so on first call call this simple method to create an IndexSearcher.
     *
     * @return
     * *
     * @param writerAndSearcher
     */
    protected fun createIndexSearcherOnOpeningDirectory(writerAndSearcher: IndexWriterAndSearcher<*>) {
        try {
            writerAndSearcher.createIndexSearcher(isReadOnly)
        } catch (ex: Exception) {
            log.error("Could not create IndexSearcher for EntityClass " + writerAndSearcher, ex)
        }
    }


    private fun rebuildIndex() {
        if (isReadOnly == true || isIndexReady == false) {
            return
        }

        deleteIndex()

        log.info("Going to rebuild Lucene index ...")
    }

    /**
     *
     *
     * Deletes complete Lucene index.
     * We hope you know what you are doing.
     *
     */
    fun deleteIndex() {
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
}