package net.dankito.deepthought.android.fragments

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_entities_list_view.*
import kotlinx.android.synthetic.main.fragment_entities_list_view.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.MultiSelectListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.utils.extensions.hideKeyboard
import net.dankito.deepthought.android.views.ContextHelpUtil
import net.dankito.deepthought.android.views.FullscreenRecyclerView
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.presenter.IMainViewSectionPresenter
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import javax.inject.Inject


abstract class EntitiesListViewFragment<T : BaseEntity>(private val contextualActionMenuResourceId: Int, private val onboardingTextResourceId: Int,
                                                        val optionsMenuResourceId: Int = R.menu.fragment_entities_list_view_menu, private val hasToolbarMenu: Boolean = true)
    : Fragment() {

    companion object {
        private const val IS_SEARCH_VIEW_ACTIVATED_EXTRA_NAME = "IS_SEARCH_VIEW_ACTIVATED"
        private const val LAST_SEARCH_TERM_EXTRA_NAME = "LAST_SEARCH_TERM"
    }


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine


    private var presenter: IMainViewSectionPresenter? = null

    protected var recyclerView: FullscreenRecyclerView? = null

    protected var recyclerAdapter: MultiSelectListRecyclerSwipeAdapter<T, out RecyclerView.ViewHolder>? = null

    private val contextHelpUtil = ContextHelpUtil()

    protected var lytOnboardingText: ViewGroup? = null

    protected var txtOnboardingText: TextView? = null

    protected var btnClearFilteredEntities: ImageButton? = null

    protected lateinit var arrowToFloatingActionButton: View

    protected lateinit var vwStartingWhereTranslatedTextViewOnboardingTextEnds: View

    private var entitiesToCheckForOnboardingOnViewCreation: List<T>? = null

    private var searchMenuItem: MenuItem? = null

    private var searchView: SearchView? = null

    private var searchResultTextView: TextView? = null

    private var layoutRootOriginalTopMargin = -1

    private var restoreIsSearchViewActivated = false

    private var lastSearchTermToRestore: String? = null


    var isCurrentSelectedTab = false



    protected open fun setupUI(rootView: View) { }

    abstract fun initPresenter(): IMainViewSectionPresenter


    abstract fun getListAdapter(): MultiSelectListRecyclerSwipeAdapter<T, out RecyclerView.ViewHolder>

    abstract fun listItemClicked(selectedItem: T)


    protected open fun getQueryHint(activity: Activity): String = ""

    abstract fun searchEntities(query: String)


    protected open fun clearFilteredEntities() { }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_entities_list_view, container, false)

        rootView?.let {
            lytOnboardingText = rootView.lytOnboardingText
            txtOnboardingText = rootView.txtOnboardingText

            btnClearFilteredEntities = rootView.btnClearFilteredEntities
            btnClearFilteredEntities?.setOnClickListener { clearFilteredEntities() }

            arrowToFloatingActionButton = rootView.arrowToFloatingActionButton
            vwStartingWhereTranslatedTextViewOnboardingTextEnds = rootView.vwStartingWhereTranslatedTextViewOnboardingTextEnds

            setupListView(it)

            setupUI(rootView)
        }

        setHasOptionsMenu(hasToolbarMenu)

        return rootView
    }

    private fun setupListView(rootView: View) {
        recyclerView = rootView.rcyEntities
        recyclerView?.addItemDecoration(HorizontalDividerItemDecoration(rootView.context))

        recyclerView?.disableFullscreenMode = true
//        recyclerView?.enterFullscreenModeListener = { recyclerViewEnteredFullscreenMode() }
//        recyclerView?.leaveFullscreenModeListener = { recyclerViewLeftFullscreenMode() }
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                activity.findViewById(R.id.floatingActionMenu)?.visibility = if(newState == RecyclerView.SCROLL_STATE_IDLE) View.VISIBLE else View.GONE

                super.onScrollStateChanged(recyclerView, newState)
            }
        })

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
            recyclerAdapter?.enableMultiSelectionMode(activity, contextualActionMenuResourceId, R.id.multiSelectModeAppBarLayout, false) { mode, actionItem, selectedItems ->
                actionItemSelected(mode, actionItem, selectedItems)
            }

            recyclerAdapter?.actionModeBarVisibilityListener = { isVisible -> multiSelectActionModeBarVisibilityChanged(isVisible) }
        }
    }

    protected open fun multiSelectActionModeBarVisibilityChanged(visible: Boolean) {
        activity?.findViewById(R.id.floatingActionMenu)?.visibility = if(visible) View.GONE else View.VISIBLE

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

    private fun recyclerViewEnteredFullscreenMode() {
        activity?.let { activity ->
            layoutRootOriginalTopMargin = activity.findViewById(R.id.appBarLayout)?.height ?: 0

            setLayoutForTogglingFullscreenMode(activity, View.GONE, 0)
        }
    }

    private fun recyclerViewLeftFullscreenMode() {
        activity?.let { activity ->
            setLayoutForTogglingFullscreenMode(activity, View.VISIBLE, layoutRootOriginalTopMargin)
        }
    }

    private fun setLayoutForTogglingFullscreenMode(activity: Activity, viewVisibility: Int, topMargin: Int) {
        activity.findViewById(R.id.appBarLayout)?.visibility = viewVisibility // TODO: bug here, don't show appBarLayout when in MultiSelectMode

        if(recyclerAdapter?.isInMultiSelectMode() == false) {
            if(isCurrentSelectedTab) { // don't set fab_menu's visibility when another tab fragment is currently selected
                activity.findViewById(R.id.floatingActionMenu)?.visibility = viewVisibility
            }
        }
        else {
            recyclerAdapter?.actionModeBar?.visibility = viewVisibility
            activity.findViewById(R.id.floatingActionMenu)?.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()

        presenter?.viewBecomesVisible()

        resumedFragment()
    }

    override fun onPause() {
        presenter?.viewGetsHidden()

        super.onPause()
    }


    protected open fun resumedFragment() {
        if(presenter == null) { // in some cases resumedFragment() gets called before onAttach() -> ensure presenter then gets initialized anyway
            presenter = initPresenter()
        }

        presenter?.let { presenter ->
            searchEngine.addInitializationListener {
                searchEntities(presenter.getLastSearchTerm())
            }
        }

        searchView?.let { searchView ->
            activity?.let { activity ->
                if(searchView.queryHint != getQueryHint(activity)) { // activity was null when searchView was created -> initialize now
                    initSearchView(searchView)
                }
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


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            var isSearchViewActivated = false
            searchView?.let { isSearchViewActivated = !it.isIconified }
            outState.putBoolean(IS_SEARCH_VIEW_ACTIVATED_EXTRA_NAME, isSearchViewActivated)
            outState.putString(LAST_SEARCH_TERM_EXTRA_NAME, presenter?.getLastSearchTerm())
        }

        getListAdapter().onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        getListAdapter().onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.let {
            restoreIsSearchViewActivated = savedInstanceState.getBoolean(IS_SEARCH_VIEW_ACTIVATED_EXTRA_NAME)
            lastSearchTermToRestore = savedInstanceState.getString(LAST_SEARCH_TERM_EXTRA_NAME)
        }
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

            txtOnboardingText.text = contextHelpUtil.stringUtil.getSpannedFromHtmlWithImages(context.getText(onboardingTextResourceId).toString(), context, txtOnboardingText.currentTextColor)
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
        contextHelpUtil.showContextHelp(lytContextHelp, helpTextResourceId)
    }


    private fun showCountSearchResults(entities: List<BaseEntity>) {
        searchView?.let { searchView ->
            if(searchResultTextView == null || isNotInSearchViewAnymore(searchResultTextView)) {
                addSearchResultTextViewToSearchView(searchView)
            }

            searchResultTextView?.text = resources.getQuantityString(R.plurals.search_count_results, entities.size, entities.size)
        }
    }

    // don't know why but Android re-initializes searchView when changing tabs -> check if searchResultTextView got kicked out of view hierarchy
    private fun isNotInSearchViewAnymore(searchResultTextView: TextView?): Boolean {
        return searchResultTextView?.parent != searchView?.findViewById(android.support.v7.appcompat.R.id.search_plate)
    }

    private fun addSearchResultTextViewToSearchView(searchView: SearchView) {
        searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn)?.let { searchCloseButton ->
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

            // if imeOptions aren't set like this searchView would take whole remaining screen when focused in landscape mode (see https://stackoverflow.com/questions/15296129/searchview-and-keyboard)
            val searchInput = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as? EditText
            searchInput?.imeOptions = getSearchViewImeOptions()

            searchView.setOnQueryTextListener(entitiesQueryTextListener) // move setOnQueryTextListener() behind searchView.setQuery() (in presenter?.getLastSearchTerm()?.let {})
            // as otherwise when lastSearchTerm != null onQuerySubmit gets called (and therefore e.g. tag filter applied)

            adjustSearchViewLayout(searchView)

            restoreLastSearchState(searchView)
        }
    }

    private fun restoreLastSearchState(searchView: SearchView) {
        presenter?.getLastSearchTerm()?.let { lastSearchTerm ->
            if (lastSearchTerm != Search.EmptySearchTerm) {
                searchView.isIconified = false
                searchView.setQuery(lastSearchTerm, false)
            }
        }

        if(restoreIsSearchViewActivated == true) {
            searchView.isIconified = false

            lastSearchTermToRestore?.let {
                searchView.setQuery(it, false)
            }
        }
    }

    protected open fun getSearchViewImeOptions() = (EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI)

    private fun adjustSearchViewLayout(searchView: SearchView) {
        (searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as? EditText)?.let { searchField ->
            try {
                val cursorDrawableField = TextView::class.java.getDeclaredField("mCursorDrawableRes") // textCursorDrawable is only accessible in xml, not in code -> get it via reflection
                cursorDrawableField.isAccessible = true
                cursorDrawableField.set(searchField, R.drawable.search_view_cursor_drawable) // with default style cursor is invisible -> set it to white
            } catch(ignored: Exception) { }
        }

        addSearchResultTextViewToSearchView(searchView)
    }

    protected fun hideSearchViewKeyboard() {
        searchView?.clearFocus()
        searchView?.hideKeyboard()
    }


    private val entitiesQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
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