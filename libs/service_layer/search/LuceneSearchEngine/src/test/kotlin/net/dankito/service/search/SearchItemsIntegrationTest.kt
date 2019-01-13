package net.dankito.service.search

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test


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

        getAndTestResult(createdEntities.first, itemsMustHaveTheseFiles = listOf(createdEntities.second))
    }


    private fun getAndTestResult(testResult: Item, searchTerm: String = Search.EmptySearchTerm, searchInFiles: Boolean = false, itemsMustHaveTheseFiles: Collection<FileLink> = listOf()) {
        val result = searchItems(searchTerm, searchInFiles = searchInFiles, itemsMustHaveTheseFiles = itemsMustHaveTheseFiles)


        assertThat(result, notNullValue())
        assertThat(result?.size, `is`(1))
        assertThat(result?.get(0), `is`(testResult))
    }

    private fun persistItemWithAttachedFile(countDummyItems: Int = 3): Pair<Item, FileLink> {
        val file = FileLink(File1Uri, File1Name, File1IsLocalFile)
        fileService.persist(file)

        val item = Item("Test")
        item.addAttachedFile(file)
        persist(item)


        for(i in 0 until 3) {
            persist(Item("$i"))
        }


        waitTillEntityGetsIndexed()

        return Pair(item, file)
    }

}