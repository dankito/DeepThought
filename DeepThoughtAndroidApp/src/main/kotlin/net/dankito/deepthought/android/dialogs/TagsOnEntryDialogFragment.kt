package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
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
import net.dankito.deepthought.model.LocalSettings
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.SearchEngineBase
import net.dankito.utils.ui.IDialogService
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class TagsOnEntryDialogFragment : FullscreenDialogFragment(), ITagsListView {

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

    private val unmodifiedTagsOnEntry = ArrayList<Tag>()

    private val tagsPreviewViewHelper = TagsPreviewViewHelper()

    private val contextHelpUtil = ContextHelpUtil()

    private var lastActionPressTime = Date()

    private var tagsChangedCallback: ((MutableList<Tag>) -> Unit)? = null

    private var mnApplyTagsOnEntryChanges: MenuItem? = null

    private var lytTagsPreview: FlexboxLayout? = null

    private var btnEditEntryCreateOrToggleTags: Button? = null

    private var btnEditEntryCreateOrToggleTagsState: TagsSearcherButtonState = TagsSearcherButtonState.DISABLED


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        adapter = TagsOnEntryRecyclerAdapter(presenter) { activity?.runOnUiThread { setTagsOnEntryPreviewOnUIThread(it) } }
    }


    override fun getDialogTag() = TAG

    override fun getLayoutId() = R.layout.dialog_tags_on_entry

    override fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_tags_on_entry_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }
        mnApplyTagsOnEntryChanges = rootView.toolbar.menu.findItem(R.id.mnApplyTagsOnEntryChanges)

        rootView.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        rootView.toolbar.setNavigationOnClickListener { closeDialog() }

        rootView.rcyTags.adapter = adapter
        rootView.rcyTags.addItemDecoration(HorizontalDividerItemDecoration(rootView.context))
        adapter.deleteTagListener = { tag -> deleteTag(tag) }

        lytTagsPreview = rootView.lytTagsPreview
        setTagsOnEntryPreviewOnUIThread()

        btnEditEntryCreateOrToggleTags = rootView.btnEditEntryCreateOrToggleTags
        rootView.btnEditEntryCreateOrToggleTags.setOnClickListener { handleCreateNewTagOrToggleTagsAction() }

        rootView.edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextWatcher)
        rootView.edtxtEditEntrySearchTag.setOnEditorActionListener { _, actionId, keyEvent -> handleEditEntrySearchTagAction(actionId, keyEvent) }

        rootView.edtxtEditEntrySearchTag.showKeyboardDelayed()

        setHasOptionsMenu(true)

        searchTags(Search.EmptySearchTerm)
    }

    override fun onDestroy() {
        presenter.destroy()

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


    private fun showContextHelpOnUiThread(helpTextResourceId: Int) {
        contextHelpUtil.showContextHelp(lytContextHelp, helpTextResourceId)
    }


    private fun deleteTag(tag: Tag) {
        removeTagFromEntry(tag)

        presenter.deleteTagAsync(tag)
    }

    private fun removeTagFromEntry(tag: Tag) {
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

        if(searchTerm != Search.EmptySearchTerm) { // in setupUI() - when view aren't set yet for showContextHelpOnUiThread() - searchTags() is called with an empty search term -> avoid that showContextHelpOnUiThread() gets called
            activity?.runOnUiThread { checkIfContextHelpSetTagsOnEntryHasBeenShownToUserOnUiThread() }
        }
    }

    private fun checkIfContextHelpSetTagsOnEntryHasBeenShownToUserOnUiThread() {
        val dataManager = tagService.dataManager
        val localSettings = dataManager.localSettings
        localSettings.countTagsOnEntrySearches++

        if(localSettings.countTagsOnEntrySearches >= LocalSettings.ShowSetTagsOnEntryHelpOnCountSearches && localSettings.didShowSetTagsOnEntryHelp == false) {
            localSettings.didShowSetTagsOnEntryHelp = true
            showContextHelpOnUiThread(R.string.context_help_set_tags_on_entry)

            dataManager.localSettingsUpdated()
        }
        else if(localSettings.countTagsOnEntrySearches < LocalSettings.ShowSetTagsOnEntryHelpOnCountSearches) {
            dataManager.localSettingsUpdated()
        }
    }


    private fun setButtonState() {
        val buttonState = presenter.getButtonStateForSearchResult()

        applyButtonState(buttonState)
    }

    private fun applyButtonState(state: TagsSearcherButtonState) {
        this.btnEditEntryCreateOrToggleTagsState = state

        btnEditEntryCreateOrToggleTags?.let { button ->
            button.isEnabled = state != TagsSearcherButtonState.DISABLED

            if(state == TagsSearcherButtonState.CREATE_TAG) {
                button.setText(R.string.dialog_tags_on_entry_create_tag)
            }
            else if(state == TagsSearcherButtonState.TOGGLE_TAGS) {
                button.setText(R.string.dialog_tags_on_entry_toggle_tags)
            }
        }
    }

    private fun handleCreateNewTagOrToggleTagsAction() {
        if(btnEditEntryCreateOrToggleTagsState == TagsSearcherButtonState.CREATE_TAG) {
            createNewTags()
        }
        else {
            toggleTagsOnEntry()
        }
    }

    private fun createNewTags() {
        val enteredText = edtxtEditEntrySearchTag.editableText.toString().trim()

        enteredText.split(SearchEngineBase.TagsSearchTermSeparator).map { it.trim() }.forEach { tagName ->
            createNewTag(tagName)
        }

        searchTags(enteredText)
    }

    private fun createNewTag(tagName: String) {
        val newTag = Tag(tagName)

        tagService.persist(newTag)

        adapter.tagsOnEntry.add(newTag)

        setTagsOnEntryPreviewOnUIThread()
    }

    private fun toggleTagsOnEntry() {
        presenter.toggleTagsOnEntry(adapter.tagsOnEntry)

        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
            setTagsOnEntryPreviewOnUIThread()
        }
    }

    private fun handleEditEntrySearchTagAction(actionId: Int, keyEvent: KeyEvent?): Boolean {
        if(actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_NULL && keyEvent?.action == KeyEvent.ACTION_DOWN)) {
            val previousActionPressTime = lastActionPressTime
            lastActionPressTime = Date()

            if(wasDoubleTap(lastActionPressTime, previousActionPressTime)) {
                applyChangesAndCloseDialog()
            }
            else {
                handleCreateNewTagOrToggleTagsAction()
            }

            return true
        }

        return false
    }

    private fun wasDoubleTap(currentActionPressTime: Date, previousActionPressTime: Date): Boolean {
        return currentActionPressTime.time - previousActionPressTime.time <= DoubleTapMaxDelayMillis
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)
    }

    private fun setTagsOnEntryPreviewOnUIThread(tagsOnEntry: MutableList<Tag>) {
        lytTagsPreview?.let { tagsPreviewViewHelper.showTagsPreview(it, tagsOnEntry, true) { removeTagFromEntry(it) } }

        mnApplyTagsOnEntryChanges?.isVisible = didTagsOnEntryChange(tagsOnEntry)
    }

    private fun didTagsOnEntryChange(tagsOnEntry: MutableList<Tag>): Boolean {
        if(unmodifiedTagsOnEntry.size != tagsOnEntry.size) {
            return true
        }

        val copy = ArrayList(tagsOnEntry)
        copy.removeAll(unmodifiedTagsOnEntry)
        return copy.size > 0
    }


    fun show(fragmentManager: FragmentManager, tagsOnEntry: MutableList<Tag>, tagsChangedCallback: ((MutableList<Tag>) -> Unit)?) {
        restoreDialog(tagsOnEntry, tagsChangedCallback)

        adapter.tagsOnEntry = ArrayList(tagsOnEntry) // make a copy so that original collection doesn't get manipulated
        setTagsOnEntryPreviewOnUIThread(tagsOnEntry)

        showInFullscreen(fragmentManager, false)
    }

    fun restoreDialog(tagsOnEntry: Collection<Tag>, tagsChangedCallback: ((MutableList<Tag>) -> Unit)?) {
        this.tagsChangedCallback = tagsChangedCallback

        unmodifiedTagsOnEntry.addAll(tagsOnEntry)
    }

    private fun applyChangesAndCloseDialog() {
        tagsChangedCallback?.invoke(adapter.tagsOnEntry)

        closeDialog()
    }

    override fun closeDialogOnUiThread(activity: FragmentActivity) {
        tagsChangedCallback = null

        edtxtEditEntrySearchTag?.hideKeyboard()

        super.closeDialogOnUiThread(activity)
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
            setButtonState()
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
            setTagsOnEntryPreviewOnUIThread()
        }
    }

}