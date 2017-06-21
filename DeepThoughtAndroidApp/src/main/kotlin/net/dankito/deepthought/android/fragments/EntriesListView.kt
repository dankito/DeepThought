package net.dankito.deepthought.android.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.*
import kotlinx.android.synthetic.main.fragment_tab_entries.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.EntryAdapter
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.search.ISearchEngine


class EntriesListView(private val searchEngine: ISearchEngine, private val router: IRouter) : Fragment(), IEntriesListView {

    private lateinit var presenter: EntriesListPresenter

    private val entryAdapter = EntryAdapter()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_tab_entries, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun setupUI(rootView: View?) {
        rootView?.lstEntries?.adapter = entryAdapter
        rootView?.lstEntries?.setOnItemClickListener { _, _, position, _ -> presenter.showEntry(entryAdapter.getItem(position)) }

        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        presenter = EntriesListPresenter(this, router, searchEngine)
    }

    override fun onDestroy() {
        presenter.cleanUp()

        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_tab_entries_menu, menu)

        // TODO: this is almost the same code as in TagsFragment -> merge
        // Associate searchable configuration with the SearchView
        val searchItem = menu.findItem(R.id.search)

        (searchItem?.actionView as? SearchView)?.let { searchView ->
            val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
            searchView.queryHint = activity.getString(R.string.search_hint_entries)
            searchView.setOnQueryTextListener(entriesQueryTextListener)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }


    private val entriesQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return onQueryTextChange(query)
        }

        override fun onQueryTextChange(query: String): Boolean {
            presenter.searchEntries(query)
            return true
        }
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        activity.runOnUiThread {
            entryAdapter.setItems(entries)
        }
    }

}