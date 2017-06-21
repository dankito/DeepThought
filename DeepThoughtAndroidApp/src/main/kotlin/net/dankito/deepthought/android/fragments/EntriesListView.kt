package net.dankito.deepthought.android.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import kotlinx.android.synthetic.main.fragment_tab_entries.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.search.ISearchEngine


class EntriesListView(private val searchEngine: ISearchEngine, private val router: IRouter) : MainActivityTabFragment(), IEntriesListView {

    private lateinit var presenter: EntriesListPresenter

    private val entryAdapter = EntryAdapter()


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_tab_entries
    }

    override fun setupUI(rootView: View?) {
        rootView?.lstEntries?.adapter = entryAdapter
        rootView?.lstEntries?.setOnItemClickListener { _, _, position, _ -> presenter.showEntry(entryAdapter.getItem(position)) }
    }

    override fun initPresenter(): IMainViewSectionPresenter {
        presenter = EntriesListPresenter(this, router, searchEngine)

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

}