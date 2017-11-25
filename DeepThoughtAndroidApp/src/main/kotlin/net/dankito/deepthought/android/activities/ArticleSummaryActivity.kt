package net.dankito.deepthought.android.activities

import android.app.SearchManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_article_summary.*
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.ArticleSummaryActivityParameters
import net.dankito.deepthought.android.adapter.ArticleSummaryItemRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.utils.ImageCache
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class ArticleSummaryActivity : BaseActivity() {

    companion object {
        const val EXTRACTOR_URL_INTENT_EXTRA_NAME = "EXTRACTOR_URL"
        const val LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME = "LAST_LOADED_SUMMARY"

        private val log = LoggerFactory.getLogger(ArticleSummaryActivity::class.java)
    }


    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var imageCache: ImageCache

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var dialogService: IDialogService


    @Inject
    protected lateinit var presenter: ArticleSummaryPresenter

    private var extractorConfig: ArticleSummaryExtractorConfig? = null

    private val adapter: ArticleSummaryItemRecyclerAdapter

    private var recyclerViewMarginBottom: Int = -1

    private var mnLoadMore: MenuItem? = null

    private var mnSearch: MenuItem? = null

    private var searchView: SearchView? = null

    private val toolbarUtil = ToolbarUtil()


    init {
        AppComponent.component.inject(this)

        adapter = ArticleSummaryItemRecyclerAdapter(this, presenter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        if(savedInstanceState == null) {
            showParameters(getParameters() as? ArticleSummaryActivityParameters)
        }
    }

    private fun showParameters(parameters: ArticleSummaryActivityParameters?) {
        if(parameters != null) {
            restoreState(parameters.extractorConfig.url, parameters.summary)
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        restoreState(savedInstanceState.getString(EXTRACTOR_URL_INTENT_EXTRA_NAME), getAndClearState(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME) as? ArticleSummary)
    }

    private fun restoreState(extractorUrl: String?, lastLoadedSummary: ArticleSummary? = null) {
        extractorUrl?.let { initializeArticlesSummaryExtractor(it) }

        if(lastLoadedSummary != null) {
            presenter.retrievedArticleSummary(lastLoadedSummary, this.extractorConfig) // set extractorConfig and restored ArticleSummaryItems

            showArticleSummaryOnUIThread(lastLoadedSummary)
        }
        else {
            extractArticlesSummary()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString(EXTRACTOR_URL_INTENT_EXTRA_NAME, extractorConfig?.url)

        presenter.lastLoadedSummary?.let { storeState(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, it) }

        adapter.onSaveInstanceState(outState)
    }


    private fun setupUI() {
        setContentView(R.layout.activity_article_summary)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rcyArticleSummaryItems.addItemDecoration(HorizontalDividerItemDecoration(this))
        rcyArticleSummaryItems.disableFullscreenMode = true
        rcyArticleSummaryItems.enterFullscreenModeListener = { recyclerViewEnteredFullscreenMode() }
        rcyArticleSummaryItems.leaveFullscreenModeListener = { recyclerViewLeftFullscreenMode() }

        rcyArticleSummaryItems.adapter = adapter
        adapter.itemClickListener = { item -> articleClicked(item) }
        adapter.actionItemClickListener = { mode, actionItem, selectedItems -> actionItemSelected(mode, actionItem, selectedItems) }
        adapter.actionModeBarVisibilityListener = { actionModeBarVisibilityChanged(it) }
    }

    private fun recyclerViewEnteredFullscreenMode() {
        if(adapter.isInMultiSelectMode()) {
            findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.visibility = View.GONE
        }
        else {
            findViewById(R.id.toolbar)?.visibility = View.GONE
        }

        recyclerViewMarginBottom = (rcyArticleSummaryItems.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: -1

        (rcyArticleSummaryItems.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
            layoutParams.bottomMargin = 0
            rcyArticleSummaryItems.layoutParams = layoutParams
        }
    }

    private fun recyclerViewLeftFullscreenMode() {
        if(adapter.isInMultiSelectMode()) {
            findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.visibility = View.VISIBLE
        }
        else {
            findViewById(R.id.toolbar)?.visibility = View.VISIBLE
        }

        if(recyclerViewMarginBottom >= 0) {
            (rcyArticleSummaryItems.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                layoutParams.bottomMargin = recyclerViewMarginBottom
                rcyArticleSummaryItems.layoutParams = layoutParams
            }
        }
    }

    private fun actionItemSelected(mode: ActionMode, actionItem: MenuItem, selectedItems: Set<ArticleSummaryItem>): Boolean {
        when(actionItem.itemId) {
            R.id.mnViewArticle -> {
                presenter.getAndShowArticlesAsync(selectedItems) {
                    finishActionModeIfAllSelectedItemsCouldBeHandled(selectedItems, mode)
                }
                return true
            }
            R.id.mnSaveArticleForLaterReading -> {
                presenter.getAndSaveArticlesForLaterReadingAsync(selectedItems) {
                    finishActionModeIfAllSelectedItemsCouldBeHandled(selectedItems, mode)
                }
                return true
            }
            R.id.mnSaveArticle -> {
                presenter.getAndSaveArticlesAsync(selectedItems) {
                    finishActionModeIfAllSelectedItemsCouldBeHandled(selectedItems, mode)
                }
                return true
            }
            R.id.mnContextualActionLoadMore -> {
                loadMoreItems()
                return true
            }
            else -> return false
        }
    }

    private fun finishActionModeIfAllSelectedItemsCouldBeHandled(selectedItems: Set<ArticleSummaryItem>, mode: ActionMode) {
        if(selectedItems.isEmpty()) {
            runOnUiThread { mode.finish() }
        }
    }

    private fun actionModeBarVisibilityChanged(isVisible: Boolean) {
        if(isVisible) {
            if(presenter.lastLoadedSummary?.canLoadMoreItems == true) {
                adapter.actionMode?.menu?.findItem(R.id.mnContextualActionLoadMore)?.isVisible = true
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_article_summary_menu, menu)

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        mnLoadMore = menu?.findItem(R.id.mnLoadMore)

        presenter.lastLoadedSummary?.let { setMenuItemLoadMore(it) }

        // Associate searchable configuration with the SearchView if available
        this.mnSearch = menu.findItem(R.id.mnSearch)
        mnSearch?.isVisible = false // TODO: just for release, re-enable afterwards again

        (mnSearch?.actionView as? SearchView)?.let { searchView ->
            this.searchView = searchView

            initSearchView(searchView)
        }

        return true
    }

    private fun initSearchView(searchView: SearchView) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))

        // if imeOptions aren't set like this searchView would take whole remaining screen when focused in landscape mode (see https://stackoverflow.com/questions/15296129/searchview-and-keyboard)
        val searchInput = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as? EditText
        searchInput?.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        searchView.setOnQueryTextListener(searchArticleSummaryItemsTextListener)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if(id == R.id.mnReload) {
            extractArticlesSummary()
            return true
        }
        else if(id == R.id.mnLoadMore) {
            loadMoreItems()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }


    override fun onBackPressed() {
        searchView?.let { searchView ->
            if(searchView.isIconified == false) { // close search view again
                searchView.setQuery("", false)
                searchView.isIconified = true
                return
            }
        }

        super.onBackPressed()
    }


    private fun initializeArticlesSummaryExtractor(extractorUrl: String) {
        try {
            extractorsConfigManager.getConfig(extractorUrl)?.let { config ->
                this.extractorConfig = config

                supportActionBar?.title = config.name

                if (config.iconUrl != null) {
                    showExtractorIcon(config)
                }
            }
        } catch(e: Exception) { }
    }

    private fun showExtractorIcon(config: ArticleSummaryExtractorConfig) {
        config.iconUrl?.let { imageCache.getCachedForRetrieveIconForUrlAsync(it) { result ->
                result.result?.let { iconPath ->
                    showExtractorIconInActionBar(iconPath, config)
                }
            }
        }
    }

    private fun showExtractorIconInActionBar(iconPath: File, config: ArticleSummaryExtractorConfig) {
        try {
            val icon = BitmapFactory.decodeFile(iconPath.path)

            val scaledIcon = getScaledIcon(icon)

            if(icon != scaledIcon) { // if icon didn't get scaled scaledIcon equals icon -> recycling would cause an app crash
                icon.recycle()
            }

            runOnUiThread {
                supportActionBar?.setIcon(BitmapDrawable(resources, scaledIcon))
            }
        } catch(e: Exception) {
            log.error("Could not load icon from url " + config.iconUrl, e)
        }
    }

    private fun getScaledIcon(icon: Bitmap): Bitmap? {
        val maxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 88f, resources.displayMetrics).toInt()
        var scaledHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        var scaledWidth = if (icon.height == 0) scaledHeight else (scaledHeight * (icon.width / icon.height.toFloat())).toInt()

        if(scaledWidth > maxWidth) { // if scaledWidth is now larger then maximum width, scale down to maxWidth
            scaledWidth = maxWidth
            scaledHeight = if (icon.width == 0) scaledWidth else (scaledWidth * (icon.height / icon.width.toFloat())).toInt()
        }

        return Bitmap.createScaledBitmap(icon, scaledWidth, scaledHeight, false)
    }

    private fun extractArticlesSummary() {
        presenter.extractArticlesSummary(extractorConfig) {
            articleSummaryReceived(it)
        }
    }

    private fun loadMoreItems() {
        presenter.lastLoadedSummary?.let { summary ->
            mnLoadMore?.isEnabled = false

            presenter.loadMoreItems(extractorConfig) {
                articleSummaryReceived(it)
            }
        }
    }

    private fun articleSummaryReceived(result: AsyncResult<out ArticleSummary>) {
        result.result?.let { showArticleSummaryThreadSafe(it)  }
    }

    private fun showArticleSummaryThreadSafe(summary: ArticleSummary) {
        runOnUiThread { showArticleSummaryOnUIThread(summary) }
    }

    private fun showArticleSummaryOnUIThread(summary: ArticleSummary) {
        setMenuItemLoadMore(summary)

        adapter.items = summary.articles

        if(summary.indexOfAddedItems > 0) {
            val centerOffset = (rcyArticleSummaryItems.height - resources.getDimension(R.dimen.list_item_read_later_article_min_height)) / 2
            (rcyArticleSummaryItems.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(summary.indexOfAddedItems, centerOffset.toInt())
        }
    }

    private fun setMenuItemLoadMore(summary: ArticleSummary) {
        mnLoadMore?.isEnabled = true
        mnLoadMore?.isVisible = summary.canLoadMoreItems
    }

    private fun articleClicked(item: ArticleSummaryItem) {
        presenter.getAndShowArticle(item)
    }


    private val searchArticleSummaryItemsTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String): Boolean {
            searchArticleSummaryItems(query)
            return true
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            return true
        }
    }

    private fun searchArticleSummaryItems(query: String) {
        presenter.lastLoadedSummary?.let { summary ->
            if(query.isBlank()) {
                adapter.items = summary.articles
            }
            else {
                searchArticleSummaryItems(query, summary)
            }
        }
    }

    private fun searchArticleSummaryItems(query: String, summary: ArticleSummary) {
        val searchResult = ArrayList<ArticleSummaryItem>()
        val lowerCaseSearchTerm = query.toLowerCase()

        summary.articles.forEach { article ->
            if (article.summary.toLowerCase().contains(lowerCaseSearchTerm) || article.title.toLowerCase().contains(lowerCaseSearchTerm)) {
                searchResult.add(article)
            }
        }

        adapter.items = searchResult
    }

}
