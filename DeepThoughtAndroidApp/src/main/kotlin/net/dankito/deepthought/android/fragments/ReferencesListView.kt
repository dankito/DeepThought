package net.dankito.deepthought.android.fragments

import android.widget.BaseAdapter
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

    private val adapter = ReferencesAdapter()


    init {
        AppComponent.component.inject(this)

        presenter = ReferencesListPresenter(this, router, searchEngine, referenceService, clipboardService)
    }


    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }

    override fun getListAdapter(): BaseAdapter {
        return adapter
    }

    override fun listItemClicked(position: Int, selectedItem: Any) {
    }

    override fun getQueryHint() = activity.getString(R.string.search_hint_references)

    override fun searchEntities(query: String) {
        presenter.searchReferences(query)
    }


    /*      IReferencesListView implementation      */

    override fun showReferences(references: List<Reference>) {
        activity?.runOnUiThread { adapter.setItems(references) }
    }

}