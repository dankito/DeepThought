package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.service.search.specific.TagsSearchResults
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class TagsSearchResultsUtilTest {

    private val underTest = TagsSearchResultsUtil()


    @Test
    fun getButtonStateForSearchResult() {
        val tag1 = Tag("1")
        val tag2 = Tag("2")
        val tag3 = Tag("3")
        val tag4 = Tag("4")
        val tag5 = Tag("5")

        val tagsOnEntry = Arrays.asList(tag1, tag2, tag3, tag4, tag5)

        val searchResults = TagsSearchResults("anything")
        searchResults.addSearchResult(createTagsSearchResult(tag1))
        searchResults.addSearchResult(createTagsSearchResult(tag2))
        searchResults.addSearchResult(createTagsSearchResult(tag3))
        searchResults.addSearchResult(createTagsSearchResult(tag4))
        searchResults.addSearchResult(createTagsSearchResult(tag5))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }


    private fun createTagsSearchResult(tag: Tag, searchTerm: String = tag.name): TagsSearchResult {
        return TagsSearchResult(searchTerm, Arrays.asList(tag))
    }

}