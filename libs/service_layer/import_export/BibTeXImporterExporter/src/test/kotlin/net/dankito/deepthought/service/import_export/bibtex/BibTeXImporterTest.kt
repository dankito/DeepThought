package net.dankito.deepthought.service.import_export.bibtex

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.SeriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.utils.IThreadPool
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File


class BibTeXImporterTest {

    private val searchEngineMock: ISearchEngine = mock()

    private val entryPersisterMock: EntryPersister = mock()

    private val tagServiceMock: TagService = mock()

    private val referencePersisterMock: ReferencePersister = mock()

    private val seriesPersisterMock: SeriesPersister = mock()

    private val threadPool: IThreadPool = mock()

    private val importer = BibTeXImporter(searchEngineMock, entryPersisterMock, tagServiceMock, referencePersisterMock, seriesPersisterMock, threadPool)


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
        val result = readTestFile("ZoteroExport.bib")

        assertThat(result.size, `is`(5))
    }

    @Test
    @Throws(Exception::class)
    fun importCitaviFile() {
        val result = readTestFile("CitaviExport.bib")

        assertThat(result.size, `is`(99))
    }


    private fun readTestFile(filename: String): Collection<Item> {
        val url = this.javaClass.classLoader.getResource("test-data/$filename")
        val bibTexFile = File(url.toURI())

        return importer.import(bibTexFile)
    }

}