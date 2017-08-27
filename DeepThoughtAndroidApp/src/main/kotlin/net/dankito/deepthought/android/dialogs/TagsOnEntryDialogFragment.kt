package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_tags_on_entry.*
import kotlinx.android.synthetic.main.dialog_tags_on_entry.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.TagsOnEntryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import java.util.*
import javax.inject.Inject


class TagsOnEntryDialogFragment : FullscreenDialogFragment(), ITagsListView {

    companion object {
        private val DoubleTapMaxDelayMillis = 500L
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

    private val adapter: TagsOnEntryAdapter

    private var lastActionPressTime = Date()

    private var tagsChangedCallback: ((MutableList<Tag>) -> Unit)? = null

    private var txtTagsPreview: TextView? = null

    private var btnEditEntryCreateOrToggleTags: Button? = null

    private var btnEditEntryCreateOrToggleTagsState: TagsSearcherButtonState = TagsSearcherButtonState.DISABLED


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        adapter = TagsOnEntryAdapter(presenter) { activity?.runOnUiThread { setTagsOnEntryPreviewOnUIThread(it) } }
    }


    override fun getLayoutId() = R.layout.dialog_tags_on_entry

    override fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_tags_on_entry_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }

        rootView.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        rootView.toolbar.setNavigationOnClickListener { closeDialog() }

        rootView.lstTags.adapter = adapter
        registerForContextMenu(rootView.lstTags)

        txtTagsPreview = rootView.txtTagsPreview
        setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)

        btnEditEntryCreateOrToggleTags = rootView.btnEditEntryCreateOrToggleTags
        rootView.btnEditEntryCreateOrToggleTags.setOnClickListener { handleCreateNewTagOrToggleTagsAction() }

        rootView.edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextWatcher)
        rootView.edtxtEditEntrySearchTag.setOnEditorActionListener { _, actionId, keyEvent -> handleEditEntrySearchTagAction(actionId, keyEvent) }

        rootView.edtxtEditEntrySearchTag.requestFocus()
        rootView.edtxtEditEntrySearchTag.postDelayed({
            val keyboard = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(rootView.edtxtEditEntrySearchTag, 0)
        }, 50)

        setHasOptionsMenu(true)

        searchTags(Search.EmptySearchTerm)
    }

    override fun onDestroy() {
        presenter.destroy()

        super.onDestroy()
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


    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_tag_menu, menu)

        // workaround as onContextItemSelected() doesn't get called in Fragment
        for(i in 0..menu.size() - 1) {
            menu.getItem(i).setOnMenuItemClickListener { item -> onContextItemSelected(item) }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position?.let { position ->
            val selectedTag = adapter.getItem(position)

            when(item.itemId) {
                R.id.mnEditTag -> {
                    presenter.editTag(selectedTag)
                    return true
                }
                R.id.mnDeleteTag -> {
                    deleteTag(selectedTag)
                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun deleteTag(tag: Tag) {
        if(adapter.tagsOnEntry.contains(tag)) {
            adapter.tagsOnEntry.remove(tag)
            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)
            }
        }

        presenter.deleteTag(tag)
    }


    private fun searchTags(searchTerm: String) {
        presenter.searchTags(searchTerm)
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
            createNewTag()
        }
        else {
            toggleTagsOnEntry()
        }
    }

    private fun createNewTag() {
        val enteredText = edtxtEditEntrySearchTag.editableText.toString()
        val newTag = Tag(enteredText)

        tagService.persist(newTag)

        adapter.tagsOnEntry.add(newTag)

        setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)

        searchTags(enteredText)
    }

    private fun toggleTagsOnEntry() {
        presenter.toggleTagsOnEntry(adapter.tagsOnEntry)

        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
            setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)
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

    private fun setTagsOnEntryPreviewOnUIThread(tagsOnEntry: MutableList<Tag>) {
        txtTagsPreview?.text = tagsOnEntry.sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }


    fun show(fragmentManager: FragmentManager, tagsOnEntry: MutableList<Tag>, tagsChangedCallback: ((MutableList<Tag>) -> Unit)?) {
        this.tagsChangedCallback = tagsChangedCallback

        adapter.tagsOnEntry = ArrayList(tagsOnEntry) // make a copy so that original collection doesn't get manipulated
        setTagsOnEntryPreviewOnUIThread(tagsOnEntry)

        showInFullscreen(fragmentManager, false)
    }

    private fun applyChangesAndCloseDialog() {
        tagsChangedCallback?.invoke(adapter.tagsOnEntry)

        closeDialog()
    }

    override fun closeDialogOnUiThread(activity: FragmentActivity) {
        tagsChangedCallback = null

        edtxtEditEntrySearchTag?.let { edtxtEditEntrySearchTag ->
            val keyboard = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(edtxtEditEntrySearchTag.windowToken, 0)
        }

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

    override fun showTags(tags: List<Tag>) {
        activity?.runOnUiThread {
            adapter.setItems(tags)
            setButtonState()
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread { adapter.notifyDataSetChanged() }
    }

}