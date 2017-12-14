package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsOnEntryListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.utils.ui.IDialogService


class TagsOnEntryListPresenter(private val tagsOnEntryListView: ITagsOnEntryListView, searchEngine: ISearchEngine, tagService: TagService, deleteEntityService: DeleteEntityService,
                               searchResultsUtil: TagsSearchResultsUtil, dialogService: IDialogService)
    : TagsListPresenterBase(tagsOnEntryListView, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService) {


    init {
        CommonComponent.component.inject(this)

        initialized()
    }


    fun getTagsFromLastSearchResult(): List<Tag> {
        val tags = ArrayList<Tag>()

        lastTagsSearchResults?.let { results ->
            results.results.forEach { result ->
                if(result.hasExactMatches()) {
                    tags.addAll(result.exactMatches)
                }
                else if(result.hasSingleMatch()) {
                    result.getSingleMatch()?.let { tags.add(it) }
                }
                else if(result.searchTerm.isNotBlank()) {
                    tags.add(Tag(result.searchTerm))
                }
            }
        }

        return tags.sortedBy { it.name }
    }

    fun getTagsSearchResultForTag(tag: Tag): TagsSearchResult? {
        lastTagsSearchResults?.let {
            it.results.forEach { result ->
                if(result.exactMatches.contains(tag) || tag == result.getSingleMatch() ||
                        (tag.isPersisted() == false && tag.name.toLowerCase() == result.searchTerm.toLowerCase())) {
                    return result
                }
            }
        }

        return null
    }

    fun getButtonStateForSearchResult(tagsOnEntry: Collection<Tag>): TagsSearcherButtonState {
        return searchResultsUtil.getButtonStateForSearchResult(lastTagsSearchResults, tagsOnEntry)
    }

    fun createNewTags(tagsOnEntry: MutableCollection<Tag>) {
        lastTagsSearchResults?.let { searchResults ->
            searchResults.results.forEach { result ->
                val term = result.searchTerm
                if(term.isNullOrBlank() == false) { // should actually never be the case
                    tagsOnEntry.add(createNewTag(term))
                }
            }

            searchTags(searchResults.overAllSearchTerm)
        }
    }

    private fun createNewTags(tagNames: Collection<String>, tagsOnEntry: MutableCollection<Tag>) {
        tagNames.forEach {
            tagsOnEntry.add(createNewTag(it))
        }

        searchTags()
    }

    private fun createNewTag(tagName: String): Tag {
        val newTag = Tag(tagName)

        tagService.persist(newTag)

        return newTag
    }

    fun toggleTagsOnEntry(tagsOnEntry: MutableCollection<Tag>, state: TagsSearcherButtonState) {
        lastTagsSearchResults?.let { searchResults ->
            val notExistingEnteredTags = ArrayList<String>()

            searchResults.results.forEach { result ->
                toggleTagOnEntry(tagsOnEntry, result, result == searchResults.lastResult, state, notExistingEnteredTags)
            }

            if(notExistingEnteredTags.isNotEmpty()) {
                handleEnteredNotExistingTags(notExistingEnteredTags)
            }
        }
    }

    private fun toggleTagOnEntry(tagsOnEntry: MutableCollection<Tag>, result: TagsSearchResult, isLastSearchResult: Boolean, state: TagsSearcherButtonState, notExistingEnteredTags: ArrayList<String>) {
        if(result.hasExactMatches()) {
            result.exactMatches.forEach { toggleTagAffiliation(it, tagsOnEntry, state) }
        }
        else if(isLastSearchResult == false && result.hasSingleMatch()) {
            result.getSingleMatch()?.let { toggleTagAffiliation(it, tagsOnEntry, state) }
        }
        else if(result.hasMatches == false || (isLastSearchResult == false && result.hasSingleMatch() == false)) {
            notExistingEnteredTags.add(result.searchTerm)
        }
        else if(isLastSearchResult) {
            result.allMatches.filterNotNull().forEach { toggleTagAffiliation(it, tagsOnEntry, state) }
        }
    }

    private fun toggleTagAffiliation(tag: Tag, tagsOnEntry: MutableCollection<Tag>, state: TagsSearcherButtonState) {
        if(tagsOnEntry.contains(tag)) {
            if(state == TagsSearcherButtonState.REMOVE_TAGS || state == TagsSearcherButtonState.TOGGLE_TAGS) {
                tagsOnEntry.remove(tag)
            }
        }
        else {
            if(state == TagsSearcherButtonState.ADD_TAGS || state == TagsSearcherButtonState.TOGGLE_TAGS) {
                tagsOnEntry.add(tag)
            }
        }
    }

    private fun handleEnteredNotExistingTags(notExistingEnteredTags: ArrayList<String>) {
        tagsOnEntryListView.shouldCreateNotExistingTags(notExistingEnteredTags) { tagsOnEntry ->
            createNewTags(notExistingEnteredTags, tagsOnEntry)
        }
    }

}