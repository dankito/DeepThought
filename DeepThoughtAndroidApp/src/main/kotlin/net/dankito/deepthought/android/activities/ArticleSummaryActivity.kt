package net.dankito.deepthought.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.AbsListView
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_article_summary.*
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ImageCache
import net.dankito.utils.serialization.ISerializer
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
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var imageCache: ImageCache

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var dialogService: IDialogService


    private var presenter: ArticleSummaryPresenter

    private var extractorConfig: ArticleSummaryExtractorConfig? = null

    private val adapter = ArticleSummaryAdapter()

    private val selectedArticlesInContextualActionMode = LinkedHashSet<ArticleSummaryItem>()

    private var mnLoadMore: MenuItem? = null


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, dialogService)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        restoreState(intent)
    }

    private fun restoreState(intent: Intent) {
        restoreState(intent.getStringExtra(EXTRACTOR_URL_INTENT_EXTRA_NAME), intent.getStringExtra(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME))
    }

    private fun restoreState(savedInstanceState: Bundle) {
        restoreState(savedInstanceState.getString(EXTRACTOR_URL_INTENT_EXTRA_NAME), savedInstanceState.getString(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME))
    }

    private fun restoreState(extractorUrl: String?, serializedLastLoadedSummary: String?) {
        extractorUrl?.let { initializeArticlesSummaryExtractor(it) }

        if(serializedLastLoadedSummary != null) {
            val summary = serializer.deserializeObject(serializedLastLoadedSummary, ArticleSummary::class.java)
            presenter.setArticleSummaryExtractorConfigOnItems(summary, this.extractorConfig) // set extractorConfig on restored ArticleSummaryItems

            showArticleSummaryOnUIThread(summary)
        }
        else {
            extractArticlesSummary()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString(EXTRACTOR_URL_INTENT_EXTRA_NAME, extractorConfig?.url)

        outState?.putString(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, null) // fallback
        presenter.lastLoadedSummary?.let { outState?.putString(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_article_summary)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lstArticleSummaryItems.adapter = adapter
        lstArticleSummaryItems.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lstArticleSummaryItems.setMultiChoiceModeListener(lstArticleSummaryItemsMultiChoiceListener)
        lstArticleSummaryItems.setOnItemClickListener { _, _, position, _ -> articleClicked(adapter.getItem(position)) }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_article_summary_menu, menu)

        mnLoadMore = menu?.findItem(R.id.mnLoadMore)

        return true
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
            val scaledSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24.toFloat(), resources.displayMetrics).toInt()
            val scaledIcon = Bitmap.createScaledBitmap(icon, scaledSize, scaledSize, false)

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
        mnLoadMore?.isEnabled = true // disable so that button cannot be pressed till loadMoreItems() result is received
        mnLoadMore?.isVisible = summary.canLoadMoreItems

        adapter.setArticleSummary(summary)

        if(summary.indexOfAddedItems > 0) {
            val centerOffset = (lstArticleSummaryItems.height - resources.getDimension(R.dimen.list_item_article_summary_item_min_height)) / 2
            lstArticleSummaryItems.smoothScrollToPositionFromTop(summary.indexOfAddedItems, centerOffset.toInt())
        }
    }

    private fun articleClicked(item: ArticleSummaryItem) {
        presenter.getAndShowArticle(item)
    }


    private val lstArticleSummaryItemsMultiChoiceListener = object: AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.activity_article_summary_contextual_action_menu, menu)

            val viewArticleItem = menu.findItem(R.id.mnViewArticle)
            viewArticleItem?.actionView?.setOnClickListener { onActionItemClicked(mode, viewArticleItem) }

            val saveArticleForLaterReadingItem = menu.findItem(R.id.mnSaveArticleForLaterReading)
            saveArticleForLaterReadingItem?.actionView?.setOnClickListener { onActionItemClicked(mode, saveArticleForLaterReadingItem) }

            val saveArticleItem = menu.findItem(R.id.mnSaveArticle)
            saveArticleItem?.actionView?.setOnClickListener { onActionItemClicked(mode, saveArticleItem) }

            placeActionModeBarInAppBarLayout()

            return true
        }

        override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
            adapter.getItem(position)?.let { item ->
                if(checked) {
                    selectedArticlesInContextualActionMode.add(item)
                }
                else {
                    selectedArticlesInContextualActionMode.remove(item)
                }
            }

            mode.title = getString(R.string.activity_article_summary_menu_count_articles_selected, selectedArticlesInContextualActionMode.size, adapter.count)
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.mnViewArticle -> {
                    presenter.getAndShowArticlesAsync(selectedArticlesInContextualActionMode) {
                        runOnUiThread { mode.finish() }
                    }
                    return true
                }
                R.id.mnSaveArticleForLaterReading -> {
                    presenter.getAndSaveArticlesForLaterReadingAsync(selectedArticlesInContextualActionMode) {
                        runOnUiThread { mode.finish() }
                    }
                    return true
                }
                R.id.mnSaveArticle -> {
                    presenter.getAndSaveArticlesAsync(selectedArticlesInContextualActionMode) {
                        runOnUiThread { mode.finish() }
                    }
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.visibility = View.GONE // hide action_mode_bar here as otherwise it will be displayed together with toolbar
            toolbar.visibility = View.VISIBLE

            selectedArticlesInContextualActionMode.clear()
        }

    }

    private fun placeActionModeBarInAppBarLayout() {
        findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.let { actionModeBar ->
            (actionModeBar.parent as? ViewGroup)?.let { parent ->
                parent.removeView(actionModeBar)
                appBarLayout.addView(actionModeBar)
                toolbar.visibility = View.GONE
            }
        }
    }

}
