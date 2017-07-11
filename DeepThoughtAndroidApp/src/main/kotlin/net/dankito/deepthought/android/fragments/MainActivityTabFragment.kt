package net.dankito.deepthought.android.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.service.search.Search


abstract class MainActivityTabFragment(private val layoutResourceId: Int, private val listViewResourceId: Int, private val optionsMenuResourceId: Int) : Fragment() {

    private lateinit var presenter: IMainViewSectionPresenter

    private var searchView: SearchView? = null


    protected open fun setupUI(rootView: View?) { }

    abstract fun initPresenter(): IMainViewSectionPresenter


    abstract fun getListAdapter(): BaseAdapter

    abstract fun listItemClicked(position: Int, selectedItem: Any)


    protected open fun getQueryHint(): String = ""

    abstract fun searchEntities(query: String)


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(layoutResourceId, container, false)

        rootView?.let { setupListView(it) }

        setHasOptionsMenu(true)

        setupUI(rootView)

        return rootView
    }

    private fun setupListView(rootView: View) {
        val listView = rootView.findViewById(listViewResourceId) as ListView

        listView.adapter = getListAdapter()
        listView.setOnItemClickListener { _, _, position, _ -> listItemClicked(position, getListAdapter().getItem(position)) }
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        presenter = initPresenter()
    }

    override fun onDestroy() {
        presenter.cleanUp()

        super.onDestroy()
    }


    fun viewCameIntoView() {
        val lastSearchTerm = presenter.getLastSearchTerm()

        if(lastSearchTerm == Search.EmptySearchTerm) {
            presenter.getAndShowAllEntities()
        }
        else {

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(optionsMenuResourceId, menu)

        // Associate searchable configuration with the SearchView if available
        val searchItem = menu.findItem(R.id.search)

        (searchItem?.actionView as? SearchView)?.let { searchView ->
            this.searchView = searchView

            initSearchView(searchView)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSearchView(searchView: SearchView) {
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView.queryHint = getQueryHint()
        searchView.setOnQueryTextListener(entriesQueryTextListener)

        val lastSearchTerm = presenter.getLastSearchTerm()
        if(lastSearchTerm != Search.EmptySearchTerm) {
            searchView.isIconified = false
            searchView.setQuery(lastSearchTerm, true)
        }
    }


    private val entriesQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return onQueryTextChange(query)
        }

        override fun onQueryTextChange(query: String): Boolean {
            searchEntities(query)
            return true
        }
    }

}