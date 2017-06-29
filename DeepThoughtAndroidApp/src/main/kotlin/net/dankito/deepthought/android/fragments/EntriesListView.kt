package net.dankito.deepthought.android.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import kotlinx.android.synthetic.main.fragment_tab_entries.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.EntryService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EntriesListView : MainActivityTabFragment(), IEntriesListView {

    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    private lateinit var presenter: EntriesListPresenter

    private lateinit var entryAdapter: EntryAdapter


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, entryService, clipboardService)
        entryAdapter = EntryAdapter(presenter)
    }


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_tab_entries
    }

    override fun setupUI(rootView: View?) {
        rootView?.lstEntries?.adapter = entryAdapter
        rootView?.lstEntries?.setOnItemClickListener { _, _, position, _ -> presenter.showEntry(entryAdapter.getItem(position)) }
    }

    override fun initPresenter(): IMainViewSectionPresenter {
        return presenter
    }


    override fun getHasOptionsMenu(): Boolean {
        return true
    }

    override fun initOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_tab_entries_menu, menu)
    }

    override fun getQueryHint() = activity.getString(R.string.search_hint_entries)

    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        activity.runOnUiThread {
            entryAdapter.setItems(entries)
        }
    }

    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        presenter.showEntriesForTag(tag, entries)
    }

}