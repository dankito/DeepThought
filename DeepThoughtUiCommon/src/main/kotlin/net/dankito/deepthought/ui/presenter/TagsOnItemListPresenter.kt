package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.tags.TagAutoCompleteResult
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsOnItemListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.utils.ui.IDialogService
import java.util.HashSet
import kotlin.collections.ArrayList


class TagsOnItemListPresenter(private val tagsOnItemListView: ITagsOnItemListView, searchEngine: ISearchEngine, tagService: TagService, deleteEntityService: DeleteEntityService,
                              searchResultsUtil: TagsSearchResultsUtil, dialogService: IDialogService)
    : TagsListPresenterBase(tagsOnItemListView, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService) {


    init {
        CommonComponent.component.inject(this)
    }


    override fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        result.lastResult?.let {
            return it.allMatches
        }

        return emptyList()
    }

    fun getTagsFromLastSearchResult(replacedTagName: String?): List<Tag> {
        val tags = ArrayList<Tag>()

        lastTagsSearchResults?.let { results ->
            results.results.forEach { result ->
                if(result.hasExactMatches()) {
                    tags.addAll(result.exactMatches)
                }
                else if(result.searchTerm.isNotBlank() && result.searchTerm != replacedTagName) {
                    tags.add(Tag(result.searchTerm))
                }
            }
        }

        return tags.sortedBy { it.name }
    }

    fun getFirstTagOfLastSearchResult(): Tag? {
        lastTagsSearchResults?.lastResult?.let { lastResult ->
            if(lastResult.searchTerm.isNotBlank()) { // disables auto complete when no search term has been entered
                if(lastResult.hasExactMatches()) {
                    return lastResult.exactMatches[0]
                }
                else if(lastResult.hasSingleMatch()) {
                    return lastResult.getSingleMatch()
                }
                else if(lastResult.allMatches.size > 0) {
                    return lastResult.allMatches[0]
                }
            }
        }

        return null
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

    fun getButtonStateForSearchResult(tagsOnItem: Collection<Tag>): TagsSearcherButtonState {
        return searchResultsUtil.getButtonStateForSearchResult(lastTagsSearchResults, tagsOnItem)
    }

    fun createNewTags(tagsOnItem: MutableCollection<Tag>) {
        lastTagsSearchResults?.let { searchResults ->
            searchResults.results.forEach { result ->
                val term = result.searchTerm
                if(term.isNullOrBlank() == false) { // should actually never be the case
                    tagsOnItem.add(createNewTag(term))
                }
            }

            searchTags(searchResults.overAllSearchTerm)
        }
    }

    private fun createNewTags(tagNames: Collection<String>, tagsOnItem: MutableCollection<Tag>) {
        tagNames.forEach {
            tagsOnItem.add(createNewTag(it))
        }

        searchTags()
    }

    private fun createNewTag(tagName: String): Tag {
        val newTag = Tag(tagName)

        tagService.persist(newTag)

        return newTag
    }

    fun toggleTagsOnItem(tagsOnItem: MutableCollection<Tag>, state: TagsSearcherButtonState) {
        lastTagsSearchResults?.let { searchResults ->
            val notExistingEnteredTags = ArrayList<String>()

            searchResults.results.forEach { result ->
                toggleTagOnItem(tagsOnItem, result, result == searchResults.lastResult, state, notExistingEnteredTags)
            }

            if(notExistingEnteredTags.isNotEmpty()) {
                handleEnteredNotExistingTags(notExistingEnteredTags)
            }
        }
    }

    private fun toggleTagOnItem(tagsOnItem: MutableCollection<Tag>, result: TagsSearchResult, isLastSearchResult: Boolean, state: TagsSearcherButtonState, notExistingEnteredTags: ArrayList<String>) {
        if(result.hasExactMatches()) {
            result.exactMatches.forEach { toggleTagAffiliation(it, tagsOnItem, state) }
        }
        else if(isLastSearchResult == false && result.hasSingleMatch()) {
            result.getSingleMatch()?.let { toggleTagAffiliation(it, tagsOnItem, state) }
        }
        else if(result.hasMatches == false || (isLastSearchResult == false && result.hasSingleMatch() == false)) {
            notExistingEnteredTags.add(result.searchTerm)
        }
        else if(isLastSearchResult) {
            result.allMatches.filterNotNull().forEach { toggleTagAffiliation(it, tagsOnItem, state) }
        }
    }

    private fun toggleTagAffiliation(tag: Tag, tagsOnItem: MutableCollection<Tag>, state: TagsSearcherButtonState) {
        if(tagsOnItem.contains(tag)) {
            if(state == TagsSearcherButtonState.REMOVE_TAGS || state == TagsSearcherButtonState.TOGGLE_TAGS) {
                tagsOnItem.remove(tag)
            }
        }
        else {
            if(state == TagsSearcherButtonState.ADD_TAGS || state == TagsSearcherButtonState.TOGGLE_TAGS) {
                tagsOnItem.add(tag)
            }
        }
    }

    private fun handleEnteredNotExistingTags(notExistingEnteredTags: ArrayList<String>) {
        tagsOnItemListView.shouldCreateNotExistingTags(notExistingEnteredTags) { tagsOnItem ->
            createNewTags(notExistingEnteredTags, tagsOnItem)
        }
    }


    fun getMergedTags(tagsOnItem: Collection<Tag>, autoCompleteResult: TagAutoCompleteResult?): Collection<Tag> {
        val tags = HashSet<Tag>()

        tags.addAll(tagsOnItem)
        tags.addAll(getTagsFromLastSearchResult(autoCompleteResult?.enteredTagNameTrimmedWithoutTagsSeparator))

        addAutoCompletedTag(tags, autoCompleteResult)

        return tags
    }

    private fun addAutoCompletedTag(tags: HashSet<Tag>, autoCompleteResult: TagAutoCompleteResult?) {
        autoCompleteResult?.let {
            tags.add(autoCompleteResult.autoCompletedTag)
        }
    }

    fun didTagsOnItemChange(originalTagsOnItem: Collection<Tag>, tagsOnItem: Collection<Tag>): Boolean {
        if(originalTagsOnItem.size != tagsOnItem.size) {
            return true
        }

        val copy = java.util.ArrayList(tagsOnItem)
        copy.removeAll(originalTagsOnItem)
        return copy.size > 0
    }


    fun autoCompleteEnteredTextForTag(enteredText: String, tag: Tag): TagAutoCompleteResult {
        var lastSearchTermStartIndex = enteredText.lastIndexOf(SearchEngineBase.TagsSearchTermSeparator)
        if(lastSearchTermStartIndex > 0) {
            if(enteredText.substring(lastSearchTermStartIndex + 1).isBlank()) { // if entered text ends with TagsSearchTermSeparator, take text before
                lastSearchTermStartIndex = enteredText.lastIndexOf(SearchEngineBase.TagsSearchTermSeparator, lastSearchTermStartIndex - 1)
            }
        }

        val replacementIndex = lastSearchTermStartIndex + 1
        val enteredTagName = enteredText.substring(replacementIndex)
        val enteredTagNameTrimmedWithoutTagsSeparator = enteredTagName.replace(SearchEngineBase.TagsSearchTermSeparator, "").trim()

        val autoCompletedTagName = (if(lastSearchTermStartIndex <= 0) "" else " ") + tag.name + SearchEngineBase.TagsSearchTermSeparator + " "
        val autoCompletedTagNameTrimmedWithoutTagsSeparator = autoCompletedTagName.replace(SearchEngineBase.TagsSearchTermSeparator, "").trim()

        val autoCompletedText = enteredText.replaceRange(replacementIndex, enteredText.length, autoCompletedTagName)

        return TagAutoCompleteResult(replacementIndex, enteredText, autoCompletedText, enteredTagName, autoCompletedTagName,
                enteredTagNameTrimmedWithoutTagsSeparator, autoCompletedTagNameTrimmedWithoutTagsSeparator, tag)
    }

}