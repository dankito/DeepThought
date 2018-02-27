package net.dankito.service.search

import net.dankito.synchronization.model.FileLink
import net.dankito.deepthought.model.Source
import net.dankito.service.search.specific.SourceSearch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class SearchSourcesIntegrationTest : LuceneSearchEngineIntegrationTestBase() {

    companion object {
        private const val File1Name = "File1"
        private const val File1Uri = "/storage/0/book.pdf"
        private const val File1IsLocalFile = true
    }


    // TODO: add tests for all the other fields


    @Test
    fun addFileToSource_SourceGetsFoundByFileName() {
        val createdEntities = persistSourceWithAttachedFile()

        getAndTestResult(createdEntities.first, File1Name)
    }

    @Test
    fun addFileToSource_SourceGetsFoundBySourcesMustHaveTheseFiles() {
        val createdEntities = persistSourceWithAttachedFile()

        getAndTestResult(createdEntities.first, mustHaveTheseFiles = listOf(createdEntities.second))
    }


    private fun getAndTestResult(testResult: Source, searchTerm: String = Search.EmptySearchTerm, mustHaveTheseFiles: Collection<FileLink> = listOf()) {
        val resultHolder = AtomicReference<List<Source>?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchSources(SourceSearch(searchTerm, mustHaveTheseFiles = mustHaveTheseFiles) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch (ignored: Exception) { }


        Assert.assertThat(resultHolder.get(), notNullValue())
        Assert.assertThat(resultHolder.get()?.size, `is`(1))
        Assert.assertThat(resultHolder.get()?.get(0), `is`(testResult))
    }

    private fun persistSourceWithAttachedFile(countDummyItems: Int = 3): Pair<Source, FileLink> {
        val file = FileLink(File1Uri, File1Name, File1IsLocalFile)
        fileService.persist(file)

        val source = Source("Test")
        source.addAttachedFile(file)
        sourceService.persist(source)


        for(i in 0 until 3) {
            sourceService.persist(Source("$i"))
        }


        waitTillEntityGetsIndexed()

        return Pair(source, file)
    }

}