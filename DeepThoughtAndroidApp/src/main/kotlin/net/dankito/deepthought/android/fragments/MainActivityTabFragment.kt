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

    private var presenter: IMainViewSectionPresenter? = null

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
        presenter?.cleanUp()

        super.onDestroy()
    }


    fun viewCameIntoView() {
        if(presenter == null) { // in some cases viewCameIntoView() gets called before onAttach() -> ensure presenter then gets initialized anyway
            presenter = initPresenter()
        }

        presenter?.let { presenter ->
            val lastSearchTerm = presenter.getLastSearchTerm()

            if(lastSearchTerm == Search.EmptySearchTerm) {
                presenter.getAndShowAllEntities()
            }
            else {

            }
        }
    }

    open fun onBackPressed(): Boolean {
        searchView?.let { searchView ->
            if(searchView.isIconified == false) { // close search view again
                searchView.setQuery("", false)
                searchView.isIconified = true
                return true
            }
        }

        return false
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

    protected open fun initSearchView(searchView: SearchView) {
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView.queryHint = getQueryHint()
        searchView.setOnQueryTextListener(entriesQueryTextListener)

        presenter?.getLastSearchTerm()?.let { lastSearchTerm ->
            if(lastSearchTerm != Search.EmptySearchTerm) {
                searchView.isIconified = false
                searchView.setQuery(lastSearchTerm, true)
            }
        }
    }


    private val entriesQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String): Boolean {
            searchEntities(query)
            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            return querySubmitted(query)
        }
    }

    protected open fun querySubmitted(query: String): Boolean {
        return true
    }

}