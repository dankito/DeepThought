package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.service.search.specific.TagsSearchResults
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class TagsSearchResultsUtilTest {

    private val underTest = TagsSearchResultsUtil()


    @Test
    fun getButtonStateForSearchResult_EmptySearchTerm_StateDisable() {
        val tagsOnEntry = listOf<Tag>()

        val searchResults = TagsSearchResults("")

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.DISABLED))
    }

    @Test
    fun getButtonStateForSearchResult_BlankSearchTerm_StateDisable() {
        val tagsOnEntry = listOf<Tag>()

        val searchResults = TagsSearchResults("                              ")

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.DISABLED))
    }


    @Test
    fun getButtonStateForSearchResult_ContainsOneNotAddedTag_StateAdd() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf<Tag>()

        val searchResults = createTagsSearchResults(tag1)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_ContainsOneAddedTag_StateRemove() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf(tag1)

        val searchResults = createTagsSearchResults(tag1)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.REMOVE_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_ContainsOneNotExistingTag_StateCreate() {
        val tagsOnEntry = listOf<Tag>()

        val searchResults = createNotExistingTagsTagsSearchResult(listOf("1"))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.CREATE_TAG))
    }


    @Test
    fun getButtonStateForSearchResult_ContainsNoAddedTag_StateAdd() {
        val tag1 = Tag("1")
        val tag2 = Tag("2")
        val tag3 = Tag("3")
        val tag4 = Tag("4")
        val tag5 = Tag("5")

        val tagsOnEntry = listOf<Tag>()

        val searchResults = createTagsSearchResults(tag1, tag2, tag3, tag4, tag5)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_ContainsOnlyAddedTags_StateRemove() {
        val tag1 = Tag("1")
        val tag2 = Tag("2")
        val tag3 = Tag("3")
        val tag4 = Tag("4")
        val tag5 = Tag("5")

        val tagsOnEntry = listOf(tag1, tag2, tag3, tag4, tag5)

        val searchResults = createTagsSearchResults(tag1, tag2, tag3, tag4, tag5)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.REMOVE_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_ContainsOnlyNotExistingTags_StateCreate() {
        val tagsOnEntry = listOf<Tag>()

        val searchResults = createNotExistingTagsTagsSearchResult(listOf("1", "2", "3", "4", "5"))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.CREATE_TAG))
    }


    @Test
    fun getButtonStateForSearchResult_OneNotAddedAndOneAddedTag_StateToggle() {
        val tag1 = Tag("1")
        val tag2 = Tag("2")

        val tagsOnEntry = listOf<Tag>(tag2)

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf(tag2)))
        searchResults.setRelevantMatchesSorted(tagsOnEntry)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.TOGGLE_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneAddedAndOneNotAddedTag_StateToggle() {
        val tag1 = Tag("1")
        val tag2 = Tag("2")

        val tagsOnEntry = listOf<Tag>(tag1)

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf(tag2)))
        searchResults.setRelevantMatchesSorted(tagsOnEntry)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.TOGGLE_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneNotAddedAndOneNotExistingTag_StateAdd() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf<Tag>()

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf()))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneNotExistingAndOneNotAddedTag_StateAdd() {
        val tag2 = Tag("2")

        val tagsOnEntry = listOf<Tag>()

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf()))
        searchResults.addSearchResult(TagsSearchResult("2", listOf(tag2)))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneAddedAndOneNotExistingTag_StateRemove() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf<Tag>(tag1)

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf()))
        searchResults.setRelevantMatchesSorted(tagsOnEntry)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.REMOVE_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneNotExistingAndOneAddedTag_StateRemove() {
        val tag2 = Tag("2")

        val tagsOnEntry = listOf<Tag>(tag2)

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf()))
        searchResults.addSearchResult(TagsSearchResult("2", listOf(tag2)))
        searchResults.setRelevantMatchesSorted(tagsOnEntry)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.REMOVE_TAGS))
    }


    @Test
    fun getButtonStateForSearchResult_OneNotAddedAndOneNotExistingTagAndWithTagsAlreadyAdded_StateAdd() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf<Tag>(Tag("noise 1"), Tag("noise 2"))

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf()))

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.ADD_TAGS))
    }

    @Test
    fun getButtonStateForSearchResult_OneAddedAndOneNotExistingTagAndWithOtherTagsAlreadyAdded_StateRemove() {
        val tag1 = Tag("1")

        val tagsOnEntry = listOf<Tag>(tag1, Tag("noise 1"), Tag("noise 2"))

        val searchResults = createcreateTagsSearchResultsForTagNames(listOf("1", "2"))
        searchResults.addSearchResult(TagsSearchResult("1", listOf(tag1)))
        searchResults.addSearchResult(TagsSearchResult("2", listOf()))
        searchResults.setRelevantMatchesSorted(tagsOnEntry)

        val state = underTest.getButtonStateForSearchResult(searchResults, tagsOnEntry)

        assertThat(state, `is`(TagsSearcherButtonState.REMOVE_TAGS))
    }


    private fun createTagsSearchResults(vararg tags: Tag): TagsSearchResults {
        val searchResults = createcreateTagsSearchResultsForTagNames(tags.map { it.name })

        tags.forEach { tag ->
            searchResults.addSearchResult(createTagsSearchResult(tag))
        }
        searchResults.setRelevantMatchesSorted(searchResults.getAllMatches())

        return searchResults
    }

    private fun createTagsSearchResult(tag: Tag, searchTerm: String = tag.name): TagsSearchResult {
        return TagsSearchResult(searchTerm, listOf(tag))
    }

    private fun createNotExistingTagsTagsSearchResult(tagNames: Collection<String>): TagsSearchResults {
        val searchResults = createcreateTagsSearchResultsForTagNames(tagNames)

        tagNames.forEach { notExistingTagName ->
            searchResults.addSearchResult(TagsSearchResult(notExistingTagName, listOf()))
        }

        return searchResults
    }

    private fun createcreateTagsSearchResultsForTagNames(tagNames: Collection<String>): TagsSearchResults {
        val searchResults = TagsSearchResults(tagNames.joinToString(SearchEngineBase.TagsSearchTermSeparator))
        searchResults.tagNamesToSearchFor = tagNames.toList()
        return searchResults
    }

}