package net.dankito.service.search

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.specific.FilteredTagsSearch
import net.dankito.service.search.specific.FilteredTagsSearchResult
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


class SearchFilteredTagsIntegrationTest : LuceneSearchEngineIntegrationTestBase() {

    @Test
    fun filter1TagWithNoOtherTagsOnEntries_Only1TagIsReturned() {
        val testData = SearchFilteredTagsTestData(1, 0, 5)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }

    @Test
    fun filter1TagWith5OtherTagsOnEntries_6TagsAreReturned() {
        val testData = SearchFilteredTagsTestData(1, 5, 5)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }


    @Test
    fun filter5TagsWithNoOtherTagsOnEntries_5TagsAreReturned() {
        val testData = SearchFilteredTagsTestData(5, 0, 15)
        createTestData(testData)


        val resultHolder = AtomicReference<FilteredTagsSearchResult>()
        executeSearch(resultHolder, testData.tagsToFilter)


        testResult(resultHolder, testData)
    }

    @Test
    fun filter5TagsWith17OtherTagsOnEntries_22TagsAreReturned() {
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

        assertThat(result.tagsOnEntriesContainingFilteredTags.size, `is`(testData.countTagsToFilter + testData.countTagsOnEntriesWithTagsToFilter))
        assertThat(result.entriesHavingFilteredTags.size, `is`(testData.countEntriesOnTagsToFilter))
    }


    private fun createTestData(testData: SearchFilteredTagsTestData) {
        testData.tagsToFilter = createTagsToFilter(testData.countTagsToFilter)

        testData.tagsOnEntriesWithTagsToFilter = createTagsOnEntriesWithTagsToFilter(testData.countTagsOnEntriesWithTagsToFilter)

        testData.entriesOnTagsToFilter = createEntriesOnTagsToFilter(testData.countEntriesOnTagsToFilter, testData.tagsToFilter, testData.tagsOnEntriesWithTagsToFilter)

        testData.noiseTags = createNoiseTags(testData.countNoiseTags)

        testData.noiseItems = createNoiseEntries(testData.countNoiseEntries, testData.noiseTags, testData.tagsOnEntriesWithTagsToFilter)
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

    private fun createTagsOnEntriesWithTagsToFilter(countTagsOnEntriesWithTagsToFilter: Int): MutableList<Tag> {
        val tagsOnEntriesWithTagsToFilter = mutableListOf<Tag>()

        for(i in 1..countTagsOnEntriesWithTagsToFilter) {
            val tagOnEntriesWithTagsToFilter = Tag("On_Entry_With_Tag_To_Filter_" + i)
            tagService.persist(tagOnEntriesWithTagsToFilter)
            tagsOnEntriesWithTagsToFilter.add(tagOnEntriesWithTagsToFilter)
        }

        return tagsOnEntriesWithTagsToFilter
    }

    private fun createEntriesOnTagsToFilter(countEntriesOnTagsToFilter: Int, tagsToFilter: List<Tag>, tagsOnEntriesWithTagsToFilter: List<Tag>): List<Item> {
        val entriesOnTagsToFilter = mutableListOf<Item>()
        val tagsOnEntriesWithTagsToFilterNoSetOnEntriesYet = ArrayList(tagsOnEntriesWithTagsToFilter)
        val tagsRandom = Random(System.nanoTime())

        for(i in 1..countEntriesOnTagsToFilter) {
            val entryOnTagToFilter = Item("Filter_" + i)

            entryOnTagToFilter.setAllTags(tagsToFilter)

            if(tagsOnEntriesWithTagsToFilter.isNotEmpty()) {
                val tagsOnEntriesWithTagsToFilterIndex = tagsRandom.nextInt(tagsOnEntriesWithTagsToFilter.size)
                val tag = tagsOnEntriesWithTagsToFilter.get(tagsOnEntriesWithTagsToFilterIndex)
                entryOnTagToFilter.addTag(tag)
                tagsOnEntriesWithTagsToFilterNoSetOnEntriesYet.remove(tag)
            }

            itemService.persist(entryOnTagToFilter)
            entriesOnTagsToFilter.add(entryOnTagToFilter)
        }

        if(tagsOnEntriesWithTagsToFilterNoSetOnEntriesYet.isNotEmpty() && entriesOnTagsToFilter.isNotEmpty()) { // if not all tags from tagsOnEntriesWithTagsToFilter have been set on items, do it now
            val entry = entriesOnTagsToFilter[0]
            tagsOnEntriesWithTagsToFilterNoSetOnEntriesYet.forEach { entry.addTag(it) }
            itemService.update(entry)
        }

        tagsToFilter.forEach { tagService.update(it) }
        tagsOnEntriesWithTagsToFilter.forEach { tagService.update(it) }

        return entriesOnTagsToFilter
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

    private fun createNoiseEntries(countNoiseEntries: Int, noiseTags: List<Tag>, tagsOnEntriesWithTagsToFilter: List<Tag>): List<Item> {
        val noiseEntries = mutableListOf<Item>()
        val tagsRandom = Random(System.nanoTime())

        for(i in 1..countNoiseEntries) {
            val noiseEntry = Item("Noise_" + i)

            if(noiseTags.isNotEmpty()) {
                val countNoiseTags = tagsRandom.nextInt(noiseTags.size)
                while (noiseEntry.countTags < countNoiseTags) {
                    noiseEntry.addTag(noiseTags.get(tagsRandom.nextInt(noiseTags.size)))
                }
            }

            if(tagsOnEntriesWithTagsToFilter.isNotEmpty()) {
                val tagsOnEntriesWithTagsToFilterIndex = tagsRandom.nextInt(tagsOnEntriesWithTagsToFilter.size)
                noiseEntry.addTag(tagsOnEntriesWithTagsToFilter.get(tagsOnEntriesWithTagsToFilterIndex))
            }

            itemService.persist(noiseEntry)
            noiseEntries.add(noiseEntry)
        }

        noiseTags.forEach { tagService.update(it) }
        tagsOnEntriesWithTagsToFilter.forEach { tagService.update(it) }

        return noiseEntries
    }

}