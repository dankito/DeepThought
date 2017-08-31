package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.text.Html
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.service.search.Search


abstract class MainActivityTabFragment(private val optionsMenuResourceId: Int, private val onboardingTextResourceId: Int) : Fragment() {

    private var presenter: IMainViewSectionPresenter? = null

    protected var listView: ListView? = null

    protected var txtOnboardingText: TextView? = null

    private var entitiesToCheckForOnboardingOnViewCreation: List<BaseEntity>? = null

    private var searchView: SearchView? = null


    protected open fun setupUI(rootView: View?) { }

    abstract fun initPresenter(): IMainViewSectionPresenter


    abstract fun getListAdapter(): BaseAdapter

    abstract fun listItemClicked(position: Int, selectedItem: Any)


    protected open fun getQueryHint(activity: Activity): String = ""

    abstract fun searchEntities(query: String)


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_main_activity_tab, container, false)

        rootView?.let {
            txtOnboardingText = rootView.txtOnboardingText
            setupListView(it)
        }

        setHasOptionsMenu(true)

        setupUI(rootView)

        return rootView
    }

    private fun setupListView(rootView: View) {
        listView = rootView.lstEntities

        listView?.adapter = getListAdapter()
        listView?.setOnItemClickListener { _, _, position, _ -> listItemClicked(position, getListAdapter().getItem(position)) }

        entitiesToCheckForOnboardingOnViewCreation?.let {
            retrievedEntitiesOnUiThread(it)
            entitiesToCheckForOnboardingOnViewCreation = null
        }
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
            searchEntities(presenter.getLastSearchTerm()) // TODO: may add a searchEngine.initializationListener
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


    protected fun retrievedEntitiesOnUiThread(entities: List<BaseEntity>) {
        if(listView == null) { // view not initialized yet
            entitiesToCheckForOnboardingOnViewCreation = entities
            return
        }

        if(shouldShowOnboardingView(entities, presenter?.getLastSearchTerm() ?: Search.EmptySearchTerm)) {
            showOnboardingView()
        }
        else {
            hideOnboardingView()
        }
    }

    protected open fun shouldShowOnboardingView(entities: List<BaseEntity>, searchTerm: String): Boolean {
        return entities.isEmpty() && searchTerm == Search.EmptySearchTerm
    }

    protected open fun showOnboardingView() {
        listView?.visibility = View.GONE

        txtOnboardingText?.let { txtOnboardingText ->
            txtOnboardingText.visibility = View.VISIBLE

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(onboardingTextResourceId).toString(), Html.FROM_HTML_MODE_LEGACY)
            }
            else {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(onboardingTextResourceId).toString())
            }
        }
    }

    protected open fun hideOnboardingView() {
        listView?.visibility = View.VISIBLE
        
        txtOnboardingText?.let { txtOnboardingText ->
            txtOnboardingText.visibility = View.GONE
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

    protected open fun initSearchView(searchView: SearchView) {
        activity?.let { activity ->
            val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
            searchView.queryHint = getQueryHint(activity)

            presenter?.getLastSearchTerm()?.let { lastSearchTerm ->
                if(lastSearchTerm != Search.EmptySearchTerm) {
                    searchView.isIconified = false
                    searchView.setQuery(lastSearchTerm, true)
                }
            }

            searchView.setOnQueryTextListener(entriesQueryTextListener) // move setOnQueryTextListener() behind searchView.setQuery() (in presenter?.getLastSearchTerm()?.let {})
            // as otherwise when lastSearchTerm != null onQuerySubmit gets called (and therefore e.g. tag filter applied)
        }
    }

    protected fun hideSearchViewKeyboard() {
        searchView?.clearFocus()
        searchView?.hideKeyboard()
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