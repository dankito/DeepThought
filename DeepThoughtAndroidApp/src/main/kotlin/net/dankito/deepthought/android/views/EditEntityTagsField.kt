package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.adapter.TagsOnItemRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnItemListPresenter
import net.dankito.deepthought.ui.tags.TagAutoCompleteResult
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsOnItemListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.util.ui.dialog.IDialogService
import java.util.*
import javax.inject.Inject


class EditEntityTagsField : EditEntityCollectionField, ITagsOnItemListView {

    companion object {
        private val DoubleTapMaxDelayMillis = 500L
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


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


    var saveChangesListener: (() -> Unit)? = null


    private val presenter: TagsOnItemListPresenter

    private val adapter: TagsOnItemRecyclerAdapter

    private var originalTagsOnItem: MutableCollection<Tag> = ArrayList<Tag>()

    private var autoCompleteResult: TagAutoCompleteResult? = null

    private var lastActionPressTime = Date()

    private var activity: BaseActivity? = null

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()




    init {
        AppComponent.component.inject(this)

        setFieldNameOnUiThread(R.string.activity_edit_item_tags_label)

        presenter = TagsOnItemListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        edtxtEntityFieldValue.setHint(R.string.activity_edit_item_edit_tags_hint)

        adapter = TagsOnItemRecyclerAdapter(presenter) { tagChange, tag, _ -> activity?.runOnUiThread { tagAddedOrRemoved(tagChange, tag) } }
        adapter.deleteTagListener = { tag -> deleteTag(tag) }

        rcySearchResult.adapter = adapter
        rcySearchResult.maxHeightInPixel = (context.resources.getDimension(R.dimen.list_item_tag_on_item_height) * 5.25).toInt() // show at max five list items and a little bit from
        // the next item so that user knows there's more

        this.disableActionOnKeyboard = false
    }


    fun setTagsToEdit(originalTagsOnItem: MutableCollection<Tag>, activity: BaseActivity) {
        this.originalTagsOnItem = originalTagsOnItem
        this.activity = activity

        adapter.tagsOnItem = LinkedHashSet(originalTagsOnItem) // make a copy so that original collection doesn't get manipulated
        setTagsOnItemPreviewOnUIThread(originalTagsOnItem)
    }


    override fun searchEntities(searchTerm: String) {
        this.autoCompleteResult = null

        presenter.searchTags(searchTerm)
    }

    override fun hasFocusChanged(hasFocus: Boolean) {
        super.hasFocusChanged(hasFocus)

        // TODO: or remove onboarding text?
        if(hasFocus == false && getMergedTags().size == 0) {
            setOnboardingTextOnUiThread(R.string.activity_edit_item_tags_onboarding_text)
        }
    }


    override fun handleActionPressed(): Boolean {
        val previousActionPressTime = lastActionPressTime
        lastActionPressTime = Date()

        if(wasDoubleTap(lastActionPressTime, previousActionPressTime)) {
            saveChangesListener?.invoke()
        }
        else {
            presenter.getFirstTagOfLastSearchResult()?.let { firstTagSearchResult ->
                addTagAndUpdatePreview(firstTagSearchResult)
            }
        }

        return true
    }

    private fun wasDoubleTap(currentActionPressTime: Date, previousActionPressTime: Date): Boolean {
        return currentActionPressTime.time - previousActionPressTime.time <= DoubleTapMaxDelayMillis
    }

    private fun tagAddedOrRemoved(tagChange: TagsOnItemRecyclerAdapter.TagChange, tag: Tag) {
        if(tagChange == TagsOnItemRecyclerAdapter.TagChange.Added) {
            addTag(tag)
        }
        else if(tagChange == TagsOnItemRecyclerAdapter.TagChange.Removed) {
            removeRemovedTagFromEnteredSearchTerm(tag)
        }

        setTagsOnItemPreviewOnUIThread()
    }

    private fun addTagAndUpdatePreview(tag: Tag) {
        addTag(tag)

        setTagsOnItemPreviewOnUIThread()
    }

    private fun addTag(tag: Tag) {
        if(this.autoCompleteResult == null) { // auto complete already applied, now an additional tag gets added but we cannot replace enteredTagName anymore
            val autoCompleteResult = presenter.autoCompleteEnteredTextForTag(getCurrentFieldValue(), tag)
            this.autoCompleteResult = autoCompleteResult
            setEditTextEntityFieldValueOnUiThread(autoCompleteResult.autoCompletedText)
        }
    }

    private fun removeRemovedTagFromEnteredSearchTerm(removedTag: Tag) {
        val autoCompleteResult = this.autoCompleteResult

        if(autoCompleteResult == null || removeLastAutoCompletedTag(autoCompleteResult, removedTag) == false) {
            removeRemovedTagFromEditTextEntityFieldValue(removedTag)
        }
    }

    private fun removeLastAutoCompletedTag(autoCompleteResult: TagAutoCompleteResult, removedTag: Tag): Boolean {
        try {
            val stringToTest = getCurrentFieldValue().substring(autoCompleteResult.replacementIndex)
            if(stringToTest == autoCompleteResult.autoCompletedTagName && autoCompleteResult.autoCompletedTagNameTrimmedWithoutTagsSeparator.contains(removedTag.name, true)) {
                setEditTextEntityFieldValueOnUiThread(autoCompleteResult.enteredText)

                this.autoCompleteResult = null // reset autoCompleteResult so if another auto completion is applied autoCompleteResult gets newly set

                return true
            }
        } catch(ignored: Exception) { }

        return false
    }

    private fun removeRemovedTagFromEditTextEntityFieldValue(removedTag: Tag) {
        presenter.getTagsSearchResultForTag(removedTag)?.let { searchResultForTag ->
            val enteredSearchTerm = edtxtEntityFieldValue.text.toString()
            val tagIndex = enteredSearchTerm.indexOf(removedTag.name, ignoreCase = true)

            if(tagIndex >= 0) {
                val indexOfNextComma = enteredSearchTerm.indexOf(SearchEngineBase.TagsSearchTermSeparator, tagIndex)
                if(indexOfNextComma > 0) {
                    setFieldValueOnUiThread(enteredSearchTerm.replaceRange(tagIndex, indexOfNextComma + 1, "").toString())
                }
                else {
                    setFieldValueOnUiThread(enteredSearchTerm.substring(0, tagIndex))
                }

                searchEntities(edtxtEntityFieldValue.text.toString())
                // TODO: may avoid that setTagsOnItemPreviewOnUIThread() gets called in tagAddedOrRemoved()
            }
        }
    }

    private fun setTagsOnItemPreviewOnUIThread() {
        setTagsOnItemPreviewOnUIThread(getMergedTags())
    }

    private fun getMergedTags(): Collection<Tag> {
        return presenter.getMergedTags(adapter.tagsOnItem, autoCompleteResult)
    }

    private fun setTagsOnItemPreviewOnUIThread(tagsOnItem: Collection<Tag>) {
        lytPreview.let { tagsPreviewViewHelper.showTagsPreview(it, tagsOnItem, showButtonRemoveTag = true) { removeTagFromCurrentTagsOnItem(it) } }

        if(adapter.items.isEmpty() || edtxtEntityFieldValue.hasFocus() == false) {
            hideSearchResultsView()
        }
        else {
            showSearchResultsView()
        }

        updateDidValueChange(presenter.didTagsOnItemChange(originalTagsOnItem, tagsOnItem))
    }


    private fun deleteTag(tag: Tag) {
        removeTagFromItem(tag)

        presenter.deleteTagAsync(tag)
    }

    private fun removeTagFromItem(tag: Tag) {
        if(originalTagsOnItem.remove(tag)) {
            updateDidValueChange(true) // so that we can notify EditItemActivity that it's tags changed
        }

        removeTagFromCurrentTagsOnItem(tag)
    }

    private fun removeTagFromCurrentTagsOnItem(tag: Tag) {
        if(adapter.tagsOnItem.contains(tag)) {
            adapter.tagsOnItem.remove(tag)
        }

        activity?.runOnUiThread {
            removeRemovedTagFromEnteredSearchTerm(tag)

            adapter.notifyDataSetChanged()
            setTagsOnItemPreviewOnUIThread()
        }
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


    /*      ITagsOnItemListView implementation         */

    override fun showEntities(entities: List<Tag>) {
        activity?.runOnUiThread {
            adapter.items = entities
            setTagsOnItemPreviewOnUIThread()
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
            setTagsOnItemPreviewOnUIThread()
        }
    }

    override fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnItem: MutableCollection<Tag>) -> Unit) {
        // we don't need to handle this anymore
    }

}