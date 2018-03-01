package net.dankito.service.search

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.specific.FilteredTagsSearch
import net.dankito.service.search.specific.FilteredTagsSearchResult
import net.dankito.synchronization.search.Search
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


class SearchFilteredTagsIntegrationTest : LuceneSearchEngineIntegrationTestBase() {

    @Test
    fun filter1TagWithNoOtherTagsOnItems_Only1TagIsReturned() {
        val testData = SearchFilteredTagsTestData(1, 0, 5)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }

    @Test
    fun filter1TagWith5OtherTagsOnItems_6TagsAreReturned() {
        val testData = SearchFilteredTagsTestData(1, 5, 5)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }


    @Test
    fun filter5TagsWithNoOtherTagsOnItems_5TagsAreReturned() {
        val testData = SearchFilteredTagsTestData(5, 0, 15)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }

    @Test
    fun filter5TagsWith17OtherTagsOnItems_22TagsAreReturned() {
        val testData = SearchFilteredTagsTestData(5, 17, 29)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }


    private fun executeSearch(resultHolder: AtomicReference<FilteredTagsSearchResult>, tagsToFilter: List<Tag>, searchTerm: String = Search.EmptySearchTerm) {
        val countDownLatch = CountDownLatch(1)

        underTest.searchFilteredTags(FilteredTagsSearch(tagsToFilter, searchTerm) {
            resultHolder.set(it)
            countDownLatch.countDown()
        })

        countDownLatch.await()
    }

    private fun testResult(resultHolder: AtomicReference<FilteredTagsSearchResult>, testData: SearchFilteredTagsTestData) {
        val result = resultHolder.get()
        assertThat(result, notNullValue())

        assertThat(result.tagsOnItemsContainingFilteredTags.size, `is`(testData.countTagsToFilter + testData.countTagsOnItemsWithTagsToFilter))
        assertThat(result.itemsHavingFilteredTags.size, `is`(testData.countItemsOnTagsToFilter))
    }


    private fun createTestData(testData: SearchFilteredTagsTestData) {
        testData.tagsToFilter = createTagsToFilter(testData.countTagsToFilter)

        testData.tagsOnItemsWithTagsToFilter = createTagsOnItemsWithTagsToFilter(testData.countTagsOnItemsWithTagsToFilter)

        testData.itemsOnTagsToFilter = createItemsOnTagsToFilter(testData.countItemsOnTagsToFilter, testData.tagsToFilter, testData.tagsOnItemsWithTagsToFilter)

        testData.noiseTags = createNoiseTags(testData.countNoiseTags)

        testData.noiseItems = createNoiseItems(testData.countNoiseItems, testData.noiseTags, testData.tagsOnItemsWithTagsToFilter)
    }

    private fun createTagsToFilter(countTagsToFilter: Int): MutableList<Tag> {
        val tagsToFilter = mutableListOf<Tag>()

        for(i in 1..countTagsToFilter) {
            val tagToFilter = Tag("Filter_" + i)
            tagService.persist(tagToFilter)
            tagsToFilter.add(tagToFilter)
        }

        return tagsToFilter
    }

    private fun createTagsOnItemsWithTagsToFilter(countTagsOnItemsWithTagsToFilter: Int): MutableList<Tag> {
        val tagsOnItemsWithTagsToFilter = mutableListOf<Tag>()

        for(i in 1..countTagsOnItemsWithTagsToFilter) {
            val tagOnItemsWithTagsToFilter = Tag("On_Item_With_Tag_To_Filter_" + i)
            tagService.persist(tagOnItemsWithTagsToFilter)
            tagsOnItemsWithTagsToFilter.add(tagOnItemsWithTagsToFilter)
        }

        return tagsOnItemsWithTagsToFilter
    }

    private fun createItemsOnTagsToFilter(countItemsOnTagsToFilter: Int, tagsToFilter: List<Tag>, tagsOnItemsWithTagsToFilter: List<Tag>): List<Item> {
        val itemsOnTagsToFilter = mutableListOf<Item>()
        val tagsOnItemsWithTagsToFilterNoSetOnItemsYet = ArrayList(tagsOnItemsWithTagsToFilter)
        val tagsRandom = Random(System.nanoTime())

        for(i in 1..countItemsOnTagsToFilter) {
            val itemOnTagToFilter = Item("Filter_" + i)

            itemOnTagToFilter.setAllTags(tagsToFilter)

            if(tagsOnItemsWithTagsToFilter.isNotEmpty()) {
                val tagsOnItemsWithTagsToFilterIndex = tagsRandom.nextInt(tagsOnItemsWithTagsToFilter.size)
                val tag = tagsOnItemsWithTagsToFilter.get(tagsOnItemsWithTagsToFilterIndex)
                itemOnTagToFilter.addTag(tag)
                tagsOnItemsWithTagsToFilterNoSetOnItemsYet.remove(tag)
            }

            itemService.persist(itemOnTagToFilter)
            itemsOnTagsToFilter.add(itemOnTagToFilter)
        }

        if(tagsOnItemsWithTagsToFilterNoSetOnItemsYet.isNotEmpty() && itemsOnTagsToFilter.isNotEmpty()) { // if not all tags from tagsOnItemsWithTagsToFilter have been set on items, do it now
            val item = itemsOnTagsToFilter[0]
            tagsOnItemsWithTagsToFilterNoSetOnItemsYet.forEach { item.addTag(it) }
            itemService.update(item)
        }

        tagsToFilter.forEach { tagService.update(it) }
        tagsOnItemsWithTagsToFilter.forEach { tagService.update(it) }

        return itemsOnTagsToFilter
    }

    private fun createNoiseTags(countNoiseTags: Int): MutableList<Tag> {
        val noiseTags = mutableListOf<Tag>()

        for(i in 1..countNoiseTags) {
            val noiseTag = Tag("Noise_" + i)
            tagService.persist(noiseTag)
            noiseTags.add(noiseTag)
        }

        return noiseTags
    }

    private fun createNoiseItems(countNoiseItems: Int, noiseTags: List<Tag>, tagsOnItemsWithTagsToFilter: List<Tag>): List<Item> {
        val noiseItems = mutableListOf<Item>()
        val tagsRandom = Random(System.nanoTime())

        for(i in 1..countNoiseItems) {
            val noiseItem = Item("Noise_" + i)

            if(noiseTags.isNotEmpty()) {
                val countNoiseTags = tagsRandom.nextInt(noiseTags.size)
                while (noiseItem.countTags < countNoiseTags) {
                    noiseItem.addTag(noiseTags.get(tagsRandom.nextInt(noiseTags.size)))
                }
            }

            if(tagsOnItemsWithTagsToFilter.isNotEmpty()) {
                val tagsOnItemsWithTagsToFilterIndex = tagsRandom.nextInt(tagsOnItemsWithTagsToFilter.size)
                noiseItem.addTag(tagsOnItemsWithTagsToFilter.get(tagsOnItemsWithTagsToFilterIndex))
            }

            itemService.persist(noiseItem)
            noiseItems.add(noiseItem)
        }

        noiseTags.forEach { tagService.update(it) }
        tagsOnItemsWithTagsToFilter.forEach { tagService.update(it) }

        return noiseItems
    }

}