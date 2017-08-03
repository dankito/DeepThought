package net.dankito.deepthought.android.fragments

import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.fragment_tab_tags.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.TagAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.FullscreenDialogFragment
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
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class TagsListView : MainActivityTabFragment(R.layout.fragment_tab_tags, R.id.lstTags, R.menu.fragment_tab_tags_menu), ITagsListView {

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

    private lateinit var adapter: TagAdapter


    init {
        AppComponent.component.inject(this)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = TagsListPresenter(this, dataManager, searchEngine, searchResultsUtil, tagService, deleteEntityService, dialogService, router)

        adapter = TagAdapter(presenter)

        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return adapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
        tagSelected(selectedItem as? Tag)
    }


    override fun setupUI(rootView: View?) {
        super.setupUI(rootView)

        rootView?.let {
            registerForContextMenu(rootView.lstTags)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_tag_menu, menu)
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
                    presenter.deleteTag(selectedTag)
                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }

        return super.onContextItemSelected(item)
    }


    override fun getQueryHint() = activity.getString(R.string.search_hint_tags)

    override fun searchEntities(query: String) {
        presenter.searchTags(query)
    }


    private fun tagSelected(selectedTag: Tag?) {
        if(selectedTag != null) {
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
        return fragmentManager.findFragmentByTag(FullscreenDialogFragment.getTag()) != null
    }


    /*          ITagsListView implementation            */

    override fun showTags(tags: List<Tag>) {
        activity?.runOnUiThread {
            adapter.setItems(tags)
        }
    }

    override fun updateDisplayedTags() {
        activity?.runOnUiThread  { adapter.notifyDataSetChanged() }
    }

}