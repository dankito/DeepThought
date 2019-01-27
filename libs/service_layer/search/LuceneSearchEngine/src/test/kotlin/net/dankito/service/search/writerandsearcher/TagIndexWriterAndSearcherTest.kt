package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.LuceneSearchEngineIntegrationTestBase
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class TagIndexWriterAndSearcherTest : LuceneSearchEngineIntegrationTestBase() {


    @Test
    fun searchTags_TagWithSpace() {
        // given
        val tagWithSpace = Tag("Mahatma Gandhi")
        persistTag(tagWithSpace)

        // when
        val result = searchTags(tagWithSpace.name)

        // then
        assertThat(result.results.size, `is`(1))
        assertThat(result.results[0].hasExactMatches(), `is`(true))
        assertThat(result.results[0].exactMatches.size, `is`(1))
        assertThat(result.results[0].exactMatches[0], `is`(tagWithSpace))
    }

    @Test
    fun searchTags_TagWithDash() {
        // given
        val tagWithDash = Tag("Hans-Georg")
        persistTag(tagWithDash)

        // when
        val result = searchTags(tagWithDash.name)

        // then
        assertThat(result.results.size, `is`(1))
        assertThat(result.results[0].hasExactMatches(), `is`(true))
        assertThat(result.results[0].exactMatches.size, `is`(1))
        assertThat(result.results[0].exactMatches[0], `is`(tagWithDash))
    }


    private fun getSearchResult(searchTerm: String = Search.EmptySearchTerm) : TagsSearchResults {
        val resultHolder = AtomicReference<TagsSearchResults?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchTags(TagsSearch(searchTerm) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try { waitForResultLatch.await(4, TimeUnit.MINUTES) } catch (ignored: Exception) { }


        assertThat(resultHolder.get(), CoreMatchers.notNullValue())

        return resultHolder.get()!!
    }

    private fun persistTag(tag: Tag, countDummyTags: Int = 3) {
        persist(tag)

        for(i in 0 until countDummyTags) {
            persist(Tag("$i"))
        }

        waitTillEntityGetsIndexed()
    }
}