package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.TagRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.TagEntriesListDialog
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.CalculatedTag
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
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


class TagsListView : MainActivityTabFragment(R.menu.fragment_tab_tags_menu, R.string.tab_tags_onboarding_text), ITagsListView {

    @Inject
    protected lateinit var dataManager: DataManager

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


    private lateinit var presenter: TagsListPresenter

    private lateinit var adapter: TagRecyclerAdapter


    init {
        AppComponent.component.inject(this)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = TagsListPresenter(this, dataManager, searchEngine, searchResultsUtil, tagService, deleteEntityService, dialogService, router)

        adapter = TagRecyclerAdapter(presenter)

        return presenter
    }

    override fun getListAdapter(): ListRecyclerSwipeAdapter<out BaseEntity, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun listItemClicked(selectedItem: BaseEntity) {
        tagSelected(selectedItem as? Tag)
    }


    override fun setupUI(rootView: View?) {
        super.setupUI(rootView)

        rootView?.let {
            registerForContextMenu(rootView.rcyEntities)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_tag_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position?.let { position ->
            if(position >= adapter.itemCount) { // couldn't believe it: if ReferenceListView is selected before TagsListView is loaded and a context menu item is selected in
                // ReferencesListView, FragmentManager.dispatchContextItemSelected()  routes onContextItemSelected() first to TagsListView -> adapter is empty, calling
                // adapter.getItem(position) crashes application
                return super.onContextItemSelected(item)
            }

            val selectedTag = adapter.getItem(position)

            when(item.itemId) {
                R.id.mnEditTag -> {
                    presenter.editTag(selectedTag)
                    return true
                }
                R.id.mnDeleteTag -> {
                    presenter.deleteTagAsync(selectedTag)
                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }

        return super.onContextItemSelected(item)
    }


    override fun initSearchView(searchView: SearchView) {
        super.initSearchView(searchView)

        searchView.imeOptions = EditorInfo.IME_ACTION_GO
    }

    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_tags)

    override fun searchEntities(query: String) {
        presenter.searchTags(query)
    }

    override fun querySubmitted(query: String): Boolean {
        presenter.toggleFilterTags(adapter.items)
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
                entities.size <= 2 && entities.filter { it is CalculatedTag == false }.isEmpty() // 2 = count of calculated tags; check first so that not over all tags is iterated
    }

    override fun showOnboardingView() {
        super.showOnboardingView()

        recyclerView?.visibility = View.VISIBLE
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Tag>) {
        activity?.runOnUiThread {
            adapter.items = entities

            retrievedEntitiesOnUiThread(entities)
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread  { adapter.notifyDataSetChanged() }
    }

}