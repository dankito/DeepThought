package net.dankito.deepthought.android.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.*
import net.dankito.deepthought.android.R


abstract class MainActivityTabFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(getLayoutResourceId(), container, false)

        setupUI(rootView)

        setHasOptionsMenu(getHasOptionsMenu())

        return rootView
    }

    protected abstract fun getLayoutResourceId(): Int

    protected abstract fun setupUI(rootView: View?)

    abstract fun initPresenter()

    abstract fun cleanUpPresenter()

    protected open fun getHasOptionsMenu(): Boolean {
        return false // may be overwritten in sub class
    }

    protected open fun initOptionsMenu(menu: Menu, inflater: MenuInflater) {

    }

    protected open fun searchEntities(query: String) {

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        initPresenter()
    }

    override fun onDestroy() {
        cleanUpPresenter()

        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(getHasOptionsMenu()) {
            initOptionsMenu(menu, inflater)

            // Associate searchable configuration with the SearchView if available
            val searchItem = menu.findItem(R.id.search)

            (searchItem?.actionView as? SearchView)?.let { searchView ->
                val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
                searchView.queryHint = activity.getString(R.string.search_hint_entries)
                searchView.setOnQueryTextListener(entriesQueryTextListener)
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
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