package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
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
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class TagsOnEntryDialogFragment : DialogFragment(), ITagsListView {

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var searchResultsUtil: TagsSearchResultsUtil

    @Inject
    protected lateinit var dialogService: IDialogService


    private val presenter: TagsOnEntryListPresenter

    private val adapter: TagsOnEntryAdapter

    private var tagsChangedCallback: ((MutableList<Tag>) -> Unit)? = null

    private var txtTagsPreview: TextView? = null

    private var btnEditEntryCreateOrToggleTags: Button? = null

    private var btnEditEntryCreateOrToggleTagsState: TagsSearcherButtonState = TagsSearcherButtonState.DISABLED


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, searchResultsUtil, dialogService)

        adapter = TagsOnEntryAdapter(presenter) { activity?.runOnUiThread { setTagsOnEntryPreviewOnUIThread(it) } }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_tags_on_entry, container, false)

        setupUI(rootView)

        setHasOptionsMenu(true)

        searchTags(Search.EmptySearchTerm)

        return rootView
    }

    private fun setupUI(rootView: View) {
        rootView.toolbar.inflateMenu(R.menu.dialog_tags_on_entry_menu)
        rootView.toolbar.setOnMenuItemClickListener { item -> menuItemClicked(item) }

        rootView.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        rootView.toolbar.setNavigationOnClickListener { closeDialog() }

        rootView.lstTags.adapter = adapter

        txtTagsPreview = rootView.txtTagsPreview
        setTagsOnEntryPreviewOnUIThread(adapter.tagsOnEntry)

        btnEditEntryCreateOrToggleTags = rootView.btnEditEntryCreateOrToggleTags
        rootView.btnEditEntryCreateOrToggleTags.setOnClickListener { handleCreateNewTagOrToggleTagsAction() }

        rootView.edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextWatcher)
        rootView.edtxtEditEntrySearchTag.setOnEditorActionListener { _, actionId, keyEvent -> handleEditEntrySearchTagAction(actionId, keyEvent) }
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
            handleCreateNewTagOrToggleTagsAction()
            return true
        }

        return false
    }

    private fun setTagsOnEntryPreviewOnUIThread(tagsOnEntry: MutableList<Tag>) {
        txtTagsPreview?.text = tagsOnEntry.sortedBy { it.name }.joinToString { it.name }
    }


    fun show(fragmentManager: FragmentManager, tagsOnEntry: MutableList<Tag>, tagsChangedCallback: ((MutableList<Tag>) -> Unit)?) {
        this.tagsChangedCallback = tagsChangedCallback

        adapter.tagsOnEntry = ArrayList(tagsOnEntry) // make a copy so that original collection doesn't get manipulated
        setTagsOnEntryPreviewOnUIThread(tagsOnEntry)

        show(fragmentManager, javaClass.name)
    }

    private fun applyChangesAndCloseDialog() {
        tagsChangedCallback?.invoke(adapter.tagsOnEntry)

        closeDialog()
    }

    private fun closeDialog() {
        tagsChangedCallback = null

        dismiss()
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