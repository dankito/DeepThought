package net.dankito.deepthought.service.import_export.bibtex

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.SeriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.utils.ThreadPool
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BibTeXImporterTest {

    companion object {
        private val log = LoggerFactory.getLogger(BibTeXImporterTest::class.java)
    }


    private val searchEngineMock: ISearchEngine = mock()

    private val entryPersisterMock: EntryPersister = mock()

    private val tagServiceMock: TagService = mock()

    private val referencePersisterMock: ReferencePersister = mock()

    private val seriesPersisterMock: SeriesPersister = mock()

    private val importer = BibTeXImporter(searchEngineMock, entryPersisterMock, tagServiceMock, referencePersisterMock, seriesPersisterMock, ThreadPool())


    @Before
    @Throws(Exception::class)
    fun setUp() {
        whenever(searchEngineMock.searchTags(any<TagsSearch>())).thenAnswer { invocation ->
            (invocation.arguments[0] as? TagsSearch)?.let { search ->
                search.searchTerm.split(SearchEngineBase.TagsSearchTermSeparator).map { it.trim() }.filter { it.isNotBlank() }.forEach { tagName ->
                    search.addResult(TagsSearchResult(tagName, ArrayList<Tag>()))
                }

                search.fireSearchCompleted()
            }
        }

        whenever(searchEngineMock.searchSeries(any<SeriesSearch>())).thenAnswer { invocation ->
            (invocation.arguments[0] as? SeriesSearch)?.let { search ->
                search.results = ArrayList<Series>()

                search.fireSearchCompleted()
            }
        }
    }


    @Test
    @Throws(Exception::class)
    fun importZoteroFile() {
        val result = importEntriesFromFile("Zotero.bib")

        assertThat(result?.size, `is`(5))
    }

    @Test
    @Throws(Exception::class)
    fun importCitaviFile() {
        val result = importEntriesFromFile("Citavi.bib")

        assertThat(result?.size, `is`(99))
    }


    private fun importEntriesFromFile(fileResourceName: String): Collection<Entry>? {
        val bibTexFile = getResourceAsFile(fileResourceName)

        if(bibTexFile == null) {
            assertThat("Could not load resource file $fileResourceName", false, `is`(true))
            return null
        }

        return importer.import(bibTexFile)
    }

    private fun getResourceAsFile(resourcePath: String): File? {
        try {
            val inputStream = BibTeXImporterTest::class.java.classLoader.getResourceAsStream(resourcePath)
            if(inputStream == null) {
                return null
            }

            val tempFile = File.createTempFile(inputStream.hashCode().toString(), ".tmp")
            tempFile.deleteOnExit()

            val outputStream = FileOutputStream(tempFile)
            //copy stream
            val buffer = ByteArray(1024)
            var bytesRead = -1

            do {
                bytesRead = inputStream.read(buffer)

                if(bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            } while(bytesRead >= 0)

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            return tempFile
        } catch (e: IOException) {
            log.error("Could not load resource file $resourcePath", e)
        }

        return null
    }

}