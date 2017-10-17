package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_main_activity_tab.*
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.TagRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.TagEntriesListDialog
import net.dankito.deepthought.model.*
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class TagsListView : MainActivityTabFragment<Tag>(R.menu.tag_contextual_action_menu, R.string.tab_tags_onboarding_text), ITagsListView {

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var searchResultsUtil: TagsSearchResultsUtil

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var allCalculatedTags: AllCalculatedTags


    private lateinit var presenter: TagsListPresenter

    private lateinit var adapter: TagRecyclerAdapter

    private lateinit var filteredTagsAdapter: TagRecyclerAdapter


    init {
        AppComponent.component.inject(this)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = TagsListPresenter(this, allCalculatedTags, searchEngine, searchResultsUtil, tagService, deleteEntityService, dialogService, router)

        adapter = TagRecyclerAdapter(presenter)

        filteredTagsAdapter = TagRecyclerAdapter(presenter)
        filteredTagsAdapter.itemClickListener = { listItemClicked(it) }
        presenter.tagFilterListener = { filteredTagsChanged(it) }

        return presenter
    }

    private fun filteredTagsChanged(filteredTags: List<Tag>) {
        filteredTagsAdapter.items = filteredTags

        activity?.runOnUiThread {
            val visibility = if(presenter.isTagFilterApplied()) View.VISIBLE else View.GONE
            lytFilteredEntities.visibility = visibility
            btnClearFilteredEntities?.visibility = visibility
        }
    }

    override fun clearFilteredEntities() {
        super.clearFilteredEntities()

        presenter.clearTagFilter()
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<Tag, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun setupUI(rootView: View) {
        super.setupUI(rootView)

        rootView.rcyFilteredEntities?.adapter = filteredTagsAdapter
    }

    override fun listItemClicked(selectedItem: Tag) {
        tagSelected(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<Tag>): Boolean {
        when(actionItem.itemId) {
            R.id.mnEditTag -> {
                selectedItems.forEach { presenter.editTag(it) }
                mode.finish()
                return true
            }
            R.id.mnDeleteTag -> {
                selectedItems.forEach { presenter.deleteTagAsync(it) }
                mode.finish()
                return true
            }
            else -> return false
        }
    }


    override fun getSearchViewImeOptions() = EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_EXTRACT_UI

    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_tags)

    override fun searchEntities(query: String) {
        presenter.searchTags(query)

        if(query != Search.EmptySearchTerm) {
            checkIfContextHelpSearchTagsHasBeenShownToUserOnUiThread()
        }
    }

    private fun checkIfContextHelpSearchTagsHasBeenShownToUserOnUiThread() {
        val localSettings = dataManager.localSettings
        localSettings.countTagSearches++

        if(localSettings.countTagSearches >= LocalSettings.ShowSearchTagsHelpOnCountSearches && localSettings.didShowSearchTagsHelp == false) {
            localSettings.didShowSearchTagsHelp = true
            showContextHelpOnUiThread(R.string.context_help_search_tags)

            dataManager.localSettingsUpdated()
        }
        else if(localSettings.countTagSearches < LocalSettings.ShowSearchTagsHelpOnCountSearches) {
            dataManager.localSettingsUpdated()
        }
    }

    override fun querySubmitted(query: String): Boolean {
        presenter.toggleFilterTagsOfLastSearchResult()
        return true
    }


    private fun tagSelected(selectedTag: Tag?) {
        if(selectedTag != null) {
            hideSearchViewKeyboard()
            presenter.showEntriesForTag(selectedTag)
        }
        else {
//            presenter.clearSelectedTag() // TODO
        }
    }


    override fun onBackPressed(): Boolean {
        if(isTagEntriesListDialogVisible()) { // let TagEntriesListDialog handle back button press
            return false
        }

        if(presenter.isTagFilterApplied()) {
            presenter.clearTagFilter()

            return true
        }

        return super.onBackPressed()
    }

    private fun isTagEntriesListDialogVisible(): Boolean {
        return fragmentManager.findFragmentByTag(TagEntriesListDialog.TAG) != null
    }


    override fun shouldShowOnboardingView(entities: List<BaseEntity>, searchTerm: String): Boolean {
        return searchTerm == Search.EmptySearchTerm && presenter.isTagFilterApplied() == false &&
                entities.size <= CalculatedTag.CountCalculatedTags &&  // check first <= count of calculated tags so that not over all tags is iterated
                entities.filter { it is CalculatedTag == false }.isEmpty()
    }

    override fun showOnboardingView() {
        super.showOnboardingView()

        recyclerView?.visibility = View.VISIBLE
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Tag>) {
        activity?.runOnUiThread {
            adapter.items = entities

            if(presenter.isTagFilterApplied()) {
                filteredTagsAdapter.notifyDataSetChanged()
            }

            retrievedEntitiesOnUiThread(entities)
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread  { adapter.notifyDataSetChanged() }
    }

}