package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.fragment_tab_references.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ReferencesAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.ReferenceService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ReferencesListView: MainActivityTabFragment(R.layout.fragment_tab_references, R.id.lstReferences, R.menu.fragment_tab_references_menu), IReferencesListView {

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    private val presenter: ReferencesListPresenter

    private val adapter: ReferencesAdapter


    init {
        AppComponent.component.inject(this)

        presenter = ReferencesListPresenter(this, router, searchEngine, referenceService, clipboardService)

        adapter = ReferencesAdapter(presenter)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return adapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
        (selectedItem as? Reference)?.let { reference ->
            presenter.showEntriesForReference(reference)
        }
    }


    override fun setupUI(rootView: View?) {
        super.setupUI(rootView)

        rootView?.let {
            registerForContextMenu(rootView.lstReferences)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        activity?.menuInflater?.inflate(R.menu.list_item_reference_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position?.let { position ->
            if(position > adapter.count) {
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


    /*      IReferencesListView implementation      */

    override fun showReferences(references: List<Reference>) {
        activity?.runOnUiThread { adapter.setItems(references) }
    }

}