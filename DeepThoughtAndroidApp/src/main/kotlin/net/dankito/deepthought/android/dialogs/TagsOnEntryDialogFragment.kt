package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.dialog_tags_on_entry.*
import kotlinx.android.synthetic.main.dialog_tags_on_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.TagsOnEntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.showKeyboardDelayed
import net.dankito.deepthought.android.views.ContextHelpUtil
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsOnEntryListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class TagsOnEntryDialogFragment : FullscreenDialogFragment(), ITagsOnEntryListView {

    companion object {
        val TAG: String = javaClass.name

        private val DoubleTapMaxDelayMillis = 500L

        private val TAGS_INTENT_EXTRA_NAME = "TAGS_ON_ENTRY"
        private val TAGS_SEPARATOR = ","
    }


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

    private var didOriginalTagsOnEntryChange = false

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()

    private val contextHelpUtil = ContextHelpUtil()

    private var lastActionPressTime = Date()

    private var tagsChangedCallback: ((Collection<Tag>) -> Unit)? = null

    private var didApplyChanges = false

    private var mnApplyTagsOnEntryChanges: MenuItem? = null

    private var lytTagsPreview: FlexboxLayout? = null


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        adapter = TagsOnEntryRecyclerAdapter(presenter) { activity?.runOnUiThread { setTagsOnEntryPreviewOnUIThread() } }
    }


    override fun getDialogTag() = TAG

    override fun getLayoutId() = R.layout.dialog_tags_on_entry

    override fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_tags_on_entry_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }
        rootView.toolbar.setNavigationOnClickListener { askIfUnsavedChangesShouldBeSavedAndCloseDialog() }
        mnApplyTagsOnEntryChanges = rootView.toolbar.menu.findItem(R.id.mnApplyTagsOnEntryChanges)

        rootView.rcyTags.adapter = adapter
        rootView.rcyTags.addItemDecoration(HorizontalDividerItemDecoration(rootView.context))
        adapter.deleteTagListener = { tag -> deleteTag(tag) }

        lytTagsPreview = rootView.lytTagsPreview
        setTagsOnEntryPreviewOnUIThread()

        rootView.edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextWatcher)
        rootView.edtxtEditEntrySearchTag.setOnEditorActionListener { _, actionId, keyEvent -> handleEditEntrySearchTagAction(actionId, keyEvent) }

        rootView.edtxtEditEntrySearchTag.showKeyboardDelayed()

        setHasOptionsMenu(true)

        searchTags(Search.EmptySearchTerm)
    }

    override fun onPause() {
        edtxtEditEntrySearchTag?.hideKeyboard()

        super.onPause()
    }

    override fun onDestroy() {
        if(didOriginalTagsOnEntryChange && didApplyChanges == false) { // tags have been deleted that are on originalTagsOnEntry, but user didn't apply changes -> inform caller now
            tagsChangedCallback?.invoke(ArrayList(originalTagsOnEntry)) // make a copy, otherwise EditEntryActivity's call to clear will also clear originalTagsOnEntry
        }

        presenter.destroy()

        tagsChangedCallback = null

        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(TAGS_INTENT_EXTRA_NAME, adapter.tagsOnEntry.joinToString(TAGS_SEPARATOR) { it.id ?: "" })
        }
    }

    override fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(TAGS_INTENT_EXTRA_NAME)?.let { tagsString ->
            val tagIds = tagsString.split(TAGS_SEPARATOR)

            tagIds.forEach { id ->
                tagService.retrieve(id)?.let { tag ->
                    adapter.tagsOnEntry.add(tag)
                }
            }

            setTagsOnEntryPreviewOnUIThread()
        }
    }


    private fun menuItemClicked(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnApplyTagsOnEntryChanges -> {
                applyChangesAndCloseDialog()
                return true
            }
        }

        return false
    }

    override fun handlesBackButtonPress(): Boolean {
        if(mnApplyTagsOnEntryChanges?.isVisible == true) {
            askIfUnsavedChangesShouldBeSaved()
            return true
        }

        return super.handlesBackButtonPress()
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(mnApplyTagsOnEntryChanges?.isVisible == true) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_apply))
        dialogService.showConfirmationDialog(getString(R.string.dialog_tags_on_item_alert_message_html_contains_unsaved_changes), config = config) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                applyChangesAndCloseDialog()
            }
            else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                closeDialog()
            }
        }
    }


    private fun showContextHelpOnUiThread(helpTextResourceId: Int) {
        contextHelpUtil.showContextHelp(lytContextHelp, helpTextResourceId)
    }


    private fun deleteTag(tag: Tag) {
        removeTagFromEntry(tag)

        presenter.deleteTagAsync(tag)
    }

    private fun removeTagFromEntry(tag: Tag) {
        if(originalTagsOnEntry.remove(tag)) {
            didOriginalTagsOnEntryChange = true // so that we can notify EditEntryActivity that it's tags changed
        }

        removeTagFromCurrentTagsOnEntry(tag)
    }

    private fun removeTagFromCurrentTagsOnEntry(tag: Tag) {
        if(adapter.tagsOnEntry.contains(tag)) {
            adapter.tagsOnEntry.remove(tag)

            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                setTagsOnEntryPreviewOnUIThread()
            }
        }
    }


    private fun searchTags(searchTerm: String) {
        presenter.searchTags(searchTerm)
    }


    private fun handleEditEntrySearchTagAction(actionId: Int, keyEvent: KeyEvent?): Boolean {
        if(actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_NULL && keyEvent?.action == KeyEvent.ACTION_DOWN)) {
            val previousActionPressTime = lastActionPressTime
            lastActionPressTime = Date()

            if(wasDoubleTap(lastActionPressTime, previousActionPressTime)) {
                applyChangesAndCloseDialog()
            }

            return true
        }

        return false
    }

    private fun wasDoubleTap(currentActionPressTime: Date, previousActionPressTime: Date): Boolean {
        return currentActionPressTime.time - previousActionPressTime.time <= DoubleTapMaxDelayMillis
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        setTagsOnEntryPreviewOnUIThread(getMergedTags())
    }

    private fun getMergedTags(): Collection<Tag> {
        val tags = HashSet<Tag>()

        tags.addAll(adapter.tagsOnEntry)
        tags.addAll(presenter.getTagsFromLastSearchResult())

        return tags
    }

    private fun setTagsOnEntryPreviewOnUIThread(tagsOnEntry: Collection<Tag>) {
        lytTagsPreview?.let { tagsPreviewViewHelper.showTagsPreview(it, tagsOnEntry, showButtonRemoveTag = true) { removeTagFromCurrentTagsOnEntry(it) } }

        mnApplyTagsOnEntryChanges?.isVisible = didTagsOnEntryChange(tagsOnEntry)
    }

    private fun didTagsOnEntryChange(tagsOnEntry: Collection<Tag>): Boolean {
        if(originalTagsOnEntry.size != tagsOnEntry.size) {
            return true
        }

        val copy = ArrayList(tagsOnEntry)
        copy.removeAll(originalTagsOnEntry)
        return copy.size > 0
    }


    fun show(fragmentManager: FragmentManager, tagsOnEntry: MutableList<Tag>, tagsChangedCallback: ((Collection<Tag>) -> Unit)?) {
        restoreDialog(tagsOnEntry, tagsChangedCallback)

        adapter.tagsOnEntry = ArrayList(tagsOnEntry) // make a copy so that original collection doesn't get manipulated
        setTagsOnEntryPreviewOnUIThread(tagsOnEntry)

        showInFullscreen(fragmentManager, false)
    }

    fun restoreDialog(tagsOnEntry: MutableCollection<Tag>, tagsChangedCallback: ((Collection<Tag>) -> Unit)?) {
        this.tagsChangedCallback = tagsChangedCallback

        originalTagsOnEntry = tagsOnEntry
    }

    private fun applyChangesAndCloseDialog() {
        didApplyChanges = true

        val tags = getMergedTags()

        tags.forEach { tag ->
            if(tag.isPersisted() == false) {
                tagService.persist(tag)
            }
        }

        tagsChangedCallback?.invoke(tags)

        closeDialog()
    }


    private val edtxtEditEntrySearchTagTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            searchTags(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }


    /*      ITagListView implementation         */

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