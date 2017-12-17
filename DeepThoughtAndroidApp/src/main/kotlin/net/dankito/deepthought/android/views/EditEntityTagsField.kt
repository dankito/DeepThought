package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.adapter.TagsOnEntryRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsOnEntryListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class EditEntityTagsField : EditEntityCollectionField, ITagsOnEntryListView {

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



    private val presenter: TagsOnEntryListPresenter

    private val adapter: TagsOnEntryRecyclerAdapter

    private var originalTagsOnEntry: MutableCollection<Tag> = ArrayList<Tag>()

    private var autoCompleteResult: TagAutoCompleteResult? = null

    private var activity: BaseActivity? = null

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()




    init {
        AppComponent.component.inject(this)

        setFieldNameOnUiThread(R.string.activity_edit_item_tags_label)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        edtxtEntityFieldValue.setHint(R.string.activity_edit_item_edit_tags_hint)

        adapter = TagsOnEntryRecyclerAdapter(presenter) { tagChange, tag, _ -> activity?.runOnUiThread { tagAddedOrRemoved(tagChange, tag) } }
        adapter.deleteTagListener = { tag -> deleteTag(tag) }

        rcySearchResult.adapter = adapter
        rcySearchResult.maxHeightInPixel = (context.resources.getDimension(R.dimen.list_item_tag_on_entry_height) * 5.25).toInt() // show at max five list items and a little bit from
        // the next item so that user knows there's more

        this.disableActionOnKeyboard = false
    }


    fun setTagsToEdit(originalTagsOnEntry: MutableCollection<Tag>, activity: BaseActivity) {
        this.originalTagsOnEntry = originalTagsOnEntry
        this.activity = activity

        adapter.tagsOnEntry = LinkedHashSet(originalTagsOnEntry) // make a copy so that original collection doesn't get manipulated
        setTagsOnEntryPreviewOnUIThread(originalTagsOnEntry)
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
        presenter.getFirstTagOfLastSearchResult()?.let { firstTagSearchResult ->
            addTag(firstTagSearchResult)
        }

        return true
    }

    private fun tagAddedOrRemoved(tagChange: TagsOnEntryRecyclerAdapter.TagChange, tag: Tag) {
        if(tagChange == TagsOnEntryRecyclerAdapter.TagChange.Added) {
            addTag(tag)
        }
        else if(tagChange == TagsOnEntryRecyclerAdapter.TagChange.Removed) {
            removeRemovedTagFromEnteredSearchTerm(tag)
        }

        setTagsOnEntryPreviewOnUIThread()
    }

    private fun addTag(tag: Tag) {
        adapter.tagsOnEntry.add(tag)

        if(this.autoCompleteResult == null) { // auto complete already applied, now an additional tag gets added but we cannot replace enteredTagName anymore
            val autoCompleteResult = tagsPreviewViewHelper.autoCompleteEnteredTextForTag(getCurrentFieldValue(), tag)
            this.autoCompleteResult = autoCompleteResult
            setEditTextEntityFieldValueOnUiThread(autoCompleteResult.autoCompletedText)
        }

        setTagsOnEntryPreviewOnUIThread()
    }

    private fun removeRemovedTagFromEnteredSearchTerm(removedTag: Tag) {
        val autoCompleteResult = this.autoCompleteResult

        if(autoCompleteResult == null || removeLastAutoCompletedTag(autoCompleteResult) == false) {
            removeRemovedTagFromEditTextEntityFieldValue(removedTag)
        }
    }

    private fun removeLastAutoCompletedTag(autoCompleteResult: TagAutoCompleteResult): Boolean {
        try {
            val stringToTest = getCurrentFieldValue().substring(autoCompleteResult.replacementIndex)
            if(stringToTest == autoCompleteResult.autoCompletedTagName) {
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
                // TODO: may avoid that setTagsOnEntryPreviewOnUIThread() gets called in tagAddedOrRemoved()
            }
        }
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        setTagsOnEntryPreviewOnUIThread(getMergedTags())
    }

    private fun getMergedTags(): Collection<Tag> {
        val tags = HashSet<Tag>()

        tags.addAll(adapter.tagsOnEntry)
        tags.addAll(presenter.getTagsFromLastSearchResult(autoCompleteResult?.enteredTagNameWithoutTagsSeparator))

        return tags
    }

    private fun setTagsOnEntryPreviewOnUIThread(tagsOnEntry: Collection<Tag>) {
        lytPreview?.let { tagsPreviewViewHelper.showTagsPreview(it, tagsOnEntry, showButtonRemoveTag = true) { removeTagFromCurrentTagsOnEntry(it) } }

        if(tagsOnEntry.isEmpty() || edtxtEntityFieldValue.hasFocus() == false) {
            hideSearchResultsView()
        }
        else {
            showSearchResultsView()
        }

        updateDidValueChange(didTagsOnEntryChange(tagsOnEntry))
    }

    private fun didTagsOnEntryChange(tagsOnEntry: Collection<Tag>): Boolean {
        if(originalTagsOnEntry.size != tagsOnEntry.size) {
            return true
        }

        val copy = ArrayList(tagsOnEntry)
        copy.removeAll(originalTagsOnEntry)
        return copy.size > 0
    }


    private fun deleteTag(tag: Tag) {
        removeTagFromEntry(tag)

        presenter.deleteTagAsync(tag)
    }

    private fun removeTagFromEntry(tag: Tag) {
        if(originalTagsOnEntry.remove(tag)) {
            updateDidValueChange(true) // so that we can notify EditEntryActivity that it's tags changed
        }

        removeTagFromCurrentTagsOnEntry(tag)
    }

    private fun removeTagFromCurrentTagsOnEntry(tag: Tag) {
        if(adapter.tagsOnEntry.contains(tag)) {
            adapter.tagsOnEntry.remove(tag)
        }

        activity?.runOnUiThread {
            removeRemovedTagFromEnteredSearchTerm(tag)

            adapter.notifyDataSetChanged()
            setTagsOnEntryPreviewOnUIThread()
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


    /*      ITagsOnEntryListView implementation         */

    override fun showEntities(entities: List<Tag>) {
        activity?.runOnUiThread {
            adapter.items = entities
            setTagsOnEntryPreviewOnUIThread()
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
            setTagsOnEntryPreviewOnUIThread()
        }
    }

    override fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnEntry: MutableCollection<Tag>) -> Unit) {
        // we don't need to handle this anymore
    }

}