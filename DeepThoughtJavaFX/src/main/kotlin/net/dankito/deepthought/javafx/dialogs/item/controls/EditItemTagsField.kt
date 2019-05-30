package net.dankito.deepthought.javafx.dialogs.item.controls

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.ui.controls.EditEntityCollectionField
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnItemListPresenter
import net.dankito.deepthought.ui.tags.TagAutoCompleteResult
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsOnItemListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.utils.extensions.sorted
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
import javax.inject.Inject


class EditItemTagsField : EditEntityCollectionField<Tag>(), ITagsOnItemListView {

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var searchResultsUtil: TagsSearchResultsUtil

    @Inject
    protected lateinit var dialogService: IDialogService


    private val presenter: TagsOnItemListPresenter

    private var autoCompleteResult: TagAutoCompleteResult? = null

    private val recycledTagViews = ArrayList<TagView>()


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnItemListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)
    }

    override fun getLabelText(): String = messages["edit.item.tags.label"]

    override fun getSearchFieldPromptText(): String {
        return messages["edit.item.tags.field.tags.prompt.text"]
    }

    override fun searchEntities(searchTerm: String) {
        this.autoCompleteResult = null

        presenter.searchTags(searchTerm)
    }

    override fun entitySelected(entity: Tag?) {
        super.entitySelected(entity)

        entity?.let {
            addTagAndUpdatePreview(entity)
        }
    }


    fun getMergedTags(): Collection<Tag> {
        return presenter.getMergedTags(editedCollection, autoCompleteResult)
    }

    override fun updateEditedCollectionPreviewOnUiThread() {
        collectionPreviewPane.children.forEach { child ->
            (child.tag as? TagView)?.let { tagView ->
                recycledTagViews.add(tagView)
            }
        }

        isUpdatingCollectionPreview = true
        collectionPreviewPane.clear()

        val sortedTags = getMergedTags().sorted()
        sortedTags.forEach { tag ->
            addTagView(tag)
        }

        isUpdatingCollectionPreview = false
        checkIfFlowPaneShouldResize()

        updateDidCollectionChange()
    }

    override fun didCollectionChange(): Boolean {
        return didCollectionChange(getMergedTags())
    }

    private fun didCollectionChange(editedCollection: Collection<Tag> = getMergedTags()): Boolean {
        return didCollectionChange(originalCollection, editedCollection)
    }

    private fun addTagView(tag: Tag) {
        if(recycledTagViews.size > 0) {
            val tagView = recycledTagViews.removeAt(0)
            tagView.update(tag.displayText) { removeTagFromCurrentTagsOnItem(tag) }

            collectionPreviewPane.add(tagView)
        }
        else {
            collectionPreviewPane.add(TagView(tag.displayText, true) { removeTagFromCurrentTagsOnItem(tag) })
        }
    }

    private fun removeTagFromCurrentTagsOnItem(tag: Tag) {
        if(editedCollection.contains(tag)) {
            editedCollection.remove(tag)
        }

        runLater {
            removeRemovedTagFromEnteredSearchTerm(tag)

//            adapter.notifyDataSetChanged() // TODO: update search results
            updateEditedCollectionPreviewOnUiThread()

            txtfldEnteredSearchTerm.requestFocus()
        }
    }


    override fun addItemToEditedCollection(item: Tag) {
        super.addItemToEditedCollection(item)

        addTag(item)
    }

    private fun addTag(tag: Tag) {
        if(this.autoCompleteResult == null) { // auto complete already applied, now an additional tag gets added but we cannot replace enteredTagName anymore
            val autoCompleteResult = presenter.autoCompleteEnteredTextForTag(enteredSearchTerm.value, tag)
            this.autoCompleteResult = autoCompleteResult
            setSearchTermWithoutCallingListenerOnUiThread(autoCompleteResult.autoCompletedText)
        }
    }

    private fun addTagAndUpdatePreview(tag: Tag) {
        addTag(tag)

        updateEditedCollectionPreviewOnUiThread()
    }

    override fun removeItemFromEditedCollection(item: Tag) {
        super.removeItemFromEditedCollection(item)

        removeRemovedTagFromEnteredSearchTerm(item)
    }

    private fun removeRemovedTagFromEnteredSearchTerm(removedTag: Tag) {
        val autoCompleteResult = this.autoCompleteResult

        if(autoCompleteResult == null || removeLastAutoCompletedTag(autoCompleteResult, removedTag) == false) {
            removeRemovedTagFromEditTextEntityFieldValue(removedTag)
        }
    }

    private fun removeLastAutoCompletedTag(autoCompleteResult: TagAutoCompleteResult, removedTag: Tag): Boolean {
        try {
            val stringToTest = enteredSearchTerm.value.substring(autoCompleteResult.replacementIndex)
            if(stringToTest == autoCompleteResult.autoCompletedTagName && autoCompleteResult.autoCompletedTagNameTrimmedWithoutTagsSeparator.contains(removedTag.name, true)) {
                setSearchTermWithoutCallingListenerOnUiThread(autoCompleteResult.enteredText)

                this.autoCompleteResult = null // reset autoCompleteResult so if another auto completion is applied autoCompleteResult gets newly set

                return true
            }
        } catch(ignored: Exception) { }

        return false
    }

    private fun removeRemovedTagFromEditTextEntityFieldValue(removedTag: Tag) {
        presenter.getTagsSearchResultForTag(removedTag)?.let { searchResultForTag ->
            val searchTerm = enteredSearchTerm.value
            val tagIndex = searchTerm.indexOf(removedTag.name, ignoreCase = true)

            if(tagIndex >= 0) {
                val indexOfNextComma = searchTerm.indexOf(SearchEngineBase.TagsSearchTermSeparator, tagIndex)
                // TODO: set caret
                if(indexOfNextComma > 0) {
                    enteredSearchTerm.value = searchTerm.replaceRange(tagIndex, indexOfNextComma + 1, "")
                }
                else {
                    enteredSearchTerm.value = searchTerm.substring(0, tagIndex)
                }

//                searchEntities(enteredSearchTerm.value) // should already be called when setting enteredSearchTerm
                // TODO: may avoid that setTagsOnItemPreviewOnUIThread() gets called in tagAddedOrRemoved()
            }
        }
    }

    private fun setSearchTermWithoutCallingListenerOnUiThread(searchTerm: String) {
        enteredSearchTerm.value = searchTerm // TODO

        txtfldEnteredSearchTerm.positionCaret(txtfldEnteredSearchTerm.text.length)
    }

    override fun getQueryToSelectFromAutoCompletionList(): String {
        presenter.lastTagsSearchResults?.let { lastTagsSearchResults ->
            lastTagsSearchResults.lastResult?.let { lastResult ->
                return lastResult.searchTerm
            }
        }

        return super.getQueryToSelectFromAutoCompletionList()
    }


    fun applyChangesAndGetTags(): Collection<Tag> {
        val tags = getMergedTags()

        tags.forEach { tag ->
            if(tag.isPersisted() == false) {
                tagService.persist(tag)
            }
        }

        return tags
    }


    override fun editEntity(entity: Tag) {
        presenter.editTag(entity)
    }

    override fun deleteEntity(entity: Tag) {
        presenter.confirmDeleteTagAsync(entity)
    }


    /*      ITagsOnItemListView implementation         */

    override fun showEntities(entities: List<Tag>) {
        updateEditedCollectionPreview()
        retrievedSearchResults(entities)
    }

    override fun updateDisplayedTags() {
        updateEditedCollectionPreview()
        // TODO: tell ListView to update its items
    }

    override fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnItem: MutableCollection<Tag>) -> Unit) {
        // we don't need to handle this anymore
    }

}