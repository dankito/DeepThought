package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.Html
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.FullScreenRecyclerView
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.service.search.Search


abstract class MainActivityTabFragment(private val optionsMenuResourceId: Int, private val onboardingTextResourceId: Int) : Fragment() {

    private var presenter: IMainViewSectionPresenter? = null

    protected var recyclerView: FullScreenRecyclerView? = null

    protected var txtOnboardingText: TextView? = null

    private var entitiesToCheckForOnboardingOnViewCreation: List<BaseEntity>? = null

    private var searchView: SearchView? = null

    private var searchResultTextView: TextView? = null

    private var layoutRootOriginalTopMargin = -1


    protected open fun setupUI(rootView: View?) { }

    abstract fun initPresenter(): IMainViewSectionPresenter


    abstract fun getListAdapter(): ListRecyclerSwipeAdapter<out BaseEntity, out RecyclerView.ViewHolder>

    abstract fun listItemClicked(selectedItem: BaseEntity)


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
        recyclerView = rootView.rcyEntities
        recyclerView?.addItemDecoration(HorizontalDividerItemDecoration(rootView.context))

        recyclerView?.hideOtherViewsListener = { recyclerViewEnteredFullScreenMode() }
        recyclerView?.showOtherViewsListener = { recyclerViewLeftFullScreenMode() }

        val adapter = getListAdapter()
        recyclerView?.adapter = adapter
        adapter.itemClickListener = { item -> listItemClicked(item)}

        entitiesToCheckForOnboardingOnViewCreation?.let {
            retrievedEntitiesOnUiThread(it)
            entitiesToCheckForOnboardingOnViewCreation = null
        }
    }

    private fun recyclerViewEnteredFullScreenMode() {
        activity?.let { activity ->
            layoutRootOriginalTopMargin = activity.findViewById(R.id.appBarLayout)?.height ?: 0

            setLayoutForTogglingFullscreenMode(activity, View.GONE, 0)
        }
    }

    private fun recyclerViewLeftFullScreenMode() {
        activity?.let { activity ->
            setLayoutForTogglingFullscreenMode(activity, View.VISIBLE, layoutRootOriginalTopMargin)
        }
    }

    private fun setLayoutForTogglingFullscreenMode(activity: Activity, viewVisibility: Int, topMargin: Int) {
        activity.findViewById(R.id.appBarLayout)?.visibility = viewVisibility
        activity.findViewById(R.id.bottomViewNavigation)?.visibility = viewVisibility
        activity.findViewById(R.id.fab_menu)?.visibility = viewVisibility

        if(topMargin >= 0) {
            (activity.findViewById(R.id.content_layout_root) as? ViewGroup)?.let { layoutRoot ->
                val layoutParams = layoutRoot.layoutParams as? ViewGroup.MarginLayoutParams

                layoutParams?.topMargin = topMargin
                layoutRoot.layoutParams = layoutParams
            }
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
        if(recyclerView == null) { // view not initialized yet
            entitiesToCheckForOnboardingOnViewCreation = entities
            return
        }

        if(shouldShowOnboardingView(entities, presenter?.getLastSearchTerm() ?: Search.EmptySearchTerm)) {
            showOnboardingView()
        }
        else {
            hideOnboardingView()
        }

        if(searchView?.isIconified == false) { // retrieved entities due to entered text in searchView
            showCountSearchResults(entities)
        }
    }

    protected open fun shouldShowOnboardingView(entities: List<BaseEntity>, searchTerm: String): Boolean {
        return entities.isEmpty() && searchTerm == Search.EmptySearchTerm
    }

    protected open fun showOnboardingView() {
        recyclerView?.visibility = View.GONE

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
        recyclerView?.visibility = View.VISIBLE
        
        txtOnboardingText?.let { txtOnboardingText ->
            txtOnboardingText.visibility = View.GONE
        }
    }

    private fun showCountSearchResults(entities: List<BaseEntity>) {
        searchView?.let { searchView ->
            if(searchResultTextView == null) {
                addSearchResultTextViewToSearchView(searchView)
            }

            searchResultTextView?.text = resources.getQuantityString(R.plurals.search_count_results, entities.size, entities.size)
        }
    }

    private fun addSearchResultTextViewToSearchView(searchView: SearchView) {
        (searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn) as? Button)?.let { searchCloseButton ->
            (searchCloseButton.parent as? LinearLayout)?.let { searchLayout ->
                val index = searchLayout.indexOfChild(searchCloseButton)

                searchResultTextView = addSearchResultTextViewToSearchView(searchLayout, searchCloseButton, index)
            }
        }
    }

    private fun addSearchResultTextViewToSearchView(searchLayout: LinearLayout, searchCloseButton: Button, index: Int): TextView {
        val searchResultTextView = TextView(searchLayout.context)

        searchResultTextView.setLines(1)
        searchResultTextView.gravity = Gravity.CENTER_VERTICAL
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            searchResultTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val layoutParams = searchCloseButton.layoutParams
        (layoutParams as? LinearLayout.LayoutParams)?.gravity = Gravity.CENTER_VERTICAL
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT

        searchLayout.addView(searchResultTextView, index, layoutParams)

        return searchResultTextView
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