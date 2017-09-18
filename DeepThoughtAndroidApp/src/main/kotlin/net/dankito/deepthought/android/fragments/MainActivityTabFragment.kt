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
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_main_activity_tab.*
import kotlinx.android.synthetic.main.fragment_main_activity_tab.view.*
import kotlinx.android.synthetic.main.layout_context_help.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.FullScreenRecyclerView
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.service.search.Search
import javax.inject.Inject


abstract class MainActivityTabFragment<T : BaseEntity>(private val contextualActionMenuResourceId: Int, private val onboardingTextResourceId: Int,
                                                       private val optionsMenuResourceId: Int = R.menu.fragment_main_activity_tab_menu) : Fragment() {

    private var presenter: IMainViewSectionPresenter? = null

    protected var recyclerView: FullScreenRecyclerView? = null

    protected var recyclerAdapter: MultiSelectListRecyclerSwipeAdapter<T, out RecyclerView.ViewHolder>? = null

    protected var lytOnboardingText: ViewGroup? = null

    protected var txtOnboardingText: TextView? = null

    private var entitiesToCheckForOnboardingOnViewCreation: List<T>? = null

    private var searchMenuItem: MenuItem? = null

    private var searchView: SearchView? = null

    private var searchResultTextView: TextView? = null

    private var layoutRootOriginalTopMargin = -1


    var isCurrentSelectedTab = false


    @Inject
    protected lateinit var dataManager: DataManager


    protected open fun setupUI(rootView: View) { }

    abstract fun initPresenter(): IMainViewSectionPresenter


    abstract fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<T, out RecyclerView.ViewHolder>

    abstract fun listItemClicked(selectedItem: T)


    protected open fun getQueryHint(activity: Activity): String = ""

    abstract fun searchEntities(query: String)


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_main_activity_tab, container, false)

        rootView?.let {
            lytOnboardingText = rootView.lytOnboardingText
            txtOnboardingText = rootView.txtOnboardingText
            setupListView(it)

            setupUI(rootView)
        }

        setHasOptionsMenu(true)

        return rootView
    }

    private fun setupListView(rootView: View) {
        recyclerView = rootView.rcyEntities
        recyclerView?.addItemDecoration(HorizontalDividerItemDecoration(rootView.context))

        recyclerView?.enterFullscreenModeListener = { recyclerViewEnteredFullScreenMode() }
        recyclerView?.leaveFullscreenModeListener = { recyclerViewLeftFullScreenMode() }

        setupAdapter()

        entitiesToCheckForOnboardingOnViewCreation?.let {
            retrievedEntitiesOnUiThread(it)
            entitiesToCheckForOnboardingOnViewCreation = null
        }
    }

    private fun setupAdapter() {
        recyclerAdapter = getListAdapter()
        recyclerView?.adapter = recyclerAdapter
        recyclerAdapter?.itemClickListener = { item -> listItemClicked(item) }
        recyclerAdapter?.swipeLayoutOpenedListener = { checkIfContextHelpListItemActionsHasBeenShownToUserOnUiThread() }

        (context as? Activity)?.let { activity ->
            recyclerAdapter?.enableMultiSelectionMode(activity, contextualActionMenuResourceId, R.id.multiSelecteModeAppBarLayout, false) { mode, actionItem, selectedItems ->
                actionItemSelected(mode, actionItem, selectedItems)
            }

            recyclerAdapter?.actionModeBarVisibilityListener = { isVisible -> multiSelectActionModeBarVisibilityChanged(isVisible) }
        }
    }

    protected open fun multiSelectActionModeBarVisibilityChanged(visible: Boolean) {
        activity?.findViewById(R.id.fab_menu)?.visibility = if(visible) View.GONE else View.VISIBLE

        if(visible) {
            checkIfContextHelpListItemActionsHasBeenShownToUserOnUiThread()
        }
    }

    private fun checkIfContextHelpListItemActionsHasBeenShownToUserOnUiThread() {
        if(dataManager.localSettings.didShowListItemActionsHelp == false) {
            showContextHelpOnUiThread(R.string.context_help_list_item_actions)

            dataManager.localSettings.didShowListItemActionsHelp = true
            dataManager.localSettingsUpdated()
        }
    }

    protected open fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<T>): Boolean {
        return false
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

        if(recyclerAdapter?.isInMultiSelectMode() == false) {
            if(isCurrentSelectedTab) { // don't set fab_menu's visibility when another tab fragment is currently selected
                activity.findViewById(R.id.fab_menu)?.visibility = viewVisibility
            }
        }
        else {
            recyclerAdapter?.actionModeBar?.visibility = viewVisibility
            activity.findViewById(R.id.fab_menu)?.visibility = View.GONE
        }

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


    protected fun retrievedEntitiesOnUiThread(entities: List<T>) {
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
            lytOnboardingText?.visibility = View.VISIBLE

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(onboardingTextResourceId).toString(), Html.FROM_HTML_MODE_LEGACY)
            }
            else {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(onboardingTextResourceId).toString())
            }
        }

        searchMenuItem?.isVisible = false
    }

    protected open fun hideOnboardingView() {
        recyclerView?.visibility = View.VISIBLE

        lytOnboardingText?.let { lytOnboardingText ->
            lytOnboardingText.visibility = View.GONE
        }

        searchMenuItem?.isVisible = true
    }


    protected fun showContextHelpOnUiThread(helpTextResourceId: Int) {
        showContextHelpOnUiThread(context.getString(helpTextResourceId))
    }

    protected fun showContextHelpOnUiThread(helpText: String) {
        txtContextHelpText.text = helpText

        lytContextHelp.visibility = View.VISIBLE

        btnDismissContextHelp.setOnClickListener { lytContextHelp.visibility = View.GONE }
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
        (searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn) as? View)?.let { searchCloseButton ->
            (searchCloseButton.parent as? LinearLayout)?.let { searchLayout ->
                val index = searchLayout.indexOfChild(searchCloseButton)

                searchResultTextView = addSearchResultTextViewToSearchView(searchLayout, searchCloseButton, index)
            }
        }
    }

    private fun addSearchResultTextViewToSearchView(searchLayout: LinearLayout, searchCloseButton: View, index: Int): TextView {
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
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(optionsMenuResourceId, menu)

        // Associate searchable configuration with the SearchView if available
        this.searchMenuItem = menu.findItem(R.id.search)

        if(lytOnboardingText?.visibility == View.VISIBLE) {
            searchMenuItem?.isVisible = false // showOnboardingView() gets called before initSearchView() on app start -> set searchView.visibility here
        }

        (searchMenuItem?.actionView as? SearchView)?.let { searchView ->
            this.searchView = searchView

            initSearchView(searchView)
        }
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