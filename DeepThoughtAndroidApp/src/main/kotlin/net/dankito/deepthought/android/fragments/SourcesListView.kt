package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.MenuItem
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.SourceRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.SourceItemsListDialog
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.SourcesListPresenter
import net.dankito.deepthought.ui.view.ISourcesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SourceService
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class SourcesListView : EntitiesListViewFragment<Source>(R.menu.source_contextual_action_menu, R.string.tab_source_onboarding_text),
        ISourcesListView {

    @Inject
    protected lateinit var sourceService: SourceService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    private val presenter: SourcesListPresenter

    private val adapter: SourceRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        presenter = SourcesListPresenter(this, searchEngine, router, clipboardService, deleteEntityService)

        adapter = SourceRecyclerAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<Source, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun listItemClicked(selectedItem: Source) {
        hideSearchViewKeyboard()
        presenter.showItemsForSource(selectedItem)
    }

    override fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<Source>): Boolean {
        when(actionItem.itemId) {
            R.id.mnEditSource -> {
                selectedItems.forEach { presenter.editSource(it) }
                mode.finish()
                return true
            }
            R.id.mnDeleteSource -> {
                selectedItems.forEach { presenter.deleteSource(it) }
                mode.finish()
                return true
            }
            else -> return false
        }
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_sources)

    override fun searchEntities(query: String) {
        presenter.searchSources(query)
    }


    override fun onBackPressed(): Boolean {
        if(isSourceItemsListDialogVisible()) { // let SourceItemsListDialog handle back button press
            return false
        }

        return super.onBackPressed()
    }

    private fun isSourceItemsListDialogVisible(): Boolean {
        return fragmentManager.findFragmentByTag(SourceItemsListDialog.TAG) != null
    }


    /*      ISourcesListView implementation      */

    override fun showEntities(entities: List<Source>) {
        activity?.runOnUiThread {
            adapter.items = entities

            retrievedEntitiesOnUiThread(entities)
        }
    }

}