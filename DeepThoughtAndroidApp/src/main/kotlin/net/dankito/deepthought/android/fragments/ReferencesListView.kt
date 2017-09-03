package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.ReferenceRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.ReferenceEntriesListDialog
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReferenceService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ReferencesListView: MainActivityTabFragment(R.menu.fragment_tab_references_menu, R.string.tab_reference_onboarding_text), IReferencesListView {

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    private val presenter: ReferencesListPresenter

    private val adapter: ReferenceRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        presenter = ReferencesListPresenter(this, router, searchEngine, referenceService, clipboardService, deleteEntityService)

        adapter = ReferenceRecyclerAdapter()
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): ListRecyclerSwipeAdapter<out BaseEntity, out RecyclerView.ViewHolder> {
        return adapter
    }

    override fun listItemClicked(selectedItem: BaseEntity) {
        (selectedItem as? Reference)?.let { reference ->
            hideSearchViewKeyboard()
            presenter.showEntriesForReference(reference)
        }
    }


    override fun setupUI(rootView: View?) {
        super.setupUI(rootView)

        rootView?.let {
            registerForContextMenu(rootView.rcyEntities)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_reference_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position?.let { position ->
            if(position >= adapter.itemCount) {
                return super.onContextItemSelected(item)
            }

            val selectedReference = adapter.getItem(position)

            when(item.itemId) {
                R.id.mnEditReference -> {
                    presenter.editReference(selectedReference)
                    return true
                }
                R.id.mnShareReferenceUrl -> {
                    presenter.copyReferenceUrlToClipboard(selectedReference)
                    return true
                }
                R.id.mnDeleteReference -> {
                    presenter.deleteReference(selectedReference)
                    return true
                }
                else -> return super.onContextItemSelected(item)
            }
        }

        return super.onContextItemSelected(item)
    }


    override fun getQueryHint(activity: Activity) = activity.getString(R.string.search_hint_references)

    override fun searchEntities(query: String) {
        presenter.searchReferences(query)
    }


    override fun onBackPressed(): Boolean {
        if(isReferenceEntriesListDialogVisible()) { // let ReferenceEntriesListDialog handle back button press
            return false
        }

        return super.onBackPressed()
    }

    private fun isReferenceEntriesListDialogVisible(): Boolean {
        return fragmentManager.findFragmentByTag(ReferenceEntriesListDialog.TAG) != null
    }


    /*      IReferencesListView implementation      */

    override fun showEntities(entities: List<Reference>) {
        activity?.runOnUiThread {
            adapter.items = entities

            retrievedEntitiesOnUiThread(entities)
        }
    }

}