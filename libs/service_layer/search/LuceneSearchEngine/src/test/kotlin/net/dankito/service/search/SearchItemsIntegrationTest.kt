package net.dankito.service.search

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import net.dankito.service.search.specific.EntriesSearch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class SearchItemsIntegrationTest : LuceneSearchEngineIntegrationTestBase() {

    companion object {
        private const val File1Name = "File1"
        private const val File1Uri = "/storage/0/book.pdf"
        private const val File1IsLocalFile = true
    }


    // TODO: add tests for all the other fields


    @Test
    fun addFileToItem_ItemGetsFoundByFileName() {
        val createdEntities = persistItemWithAttachedFile()

        getAndTestResult(createdEntities.first, File1Name, true)
    }

    @Test
    fun addFileToItem_ItemGetsFoundByItemsMustHaveTheseFiles() {
        val createdEntities = persistItemWithAttachedFile()

        getAndTestResult(createdEntities.first, entriesMustHaveTheseFiles = listOf(createdEntities.second))
    }


    private fun getAndTestResult(testResult: Item, searchTerm: String = Search.EmptySearchTerm, searchInFiles: Boolean = false, entriesMustHaveTheseFiles: Collection<FileLink> = listOf()) {
        val resultHolder = AtomicReference<List<Item>?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchEntries(EntriesSearch(searchTerm, false, false, false, false, searchInFiles,
                entriesMustHaveTheseFiles = entriesMustHaveTheseFiles) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch (ignored: Exception) { }


        assertThat(resultHolder.get(), notNullValue())
        assertThat(resultHolder.get()?.size, `is`(1))
        assertThat(resultHolder.get()?.get(0), `is`(testResult))
    }

    private fun persistItemWithAttachedFile(countDummyItems: Int = 3): Pair<Item, FileLink> {
        val file = FileLink(File1Uri, File1Name, File1IsLocalFile)
        fileService.persist(file)

        val item = Item("Test")
        item.addAttachedFile(file)
        entryService.persist(item)


        for(i in 0 until 3) {
            entryService.persist(Item("$i"))
        }


        waitTillEntityGetsIndexed()

        return Pair(item, file)
    }

}