package net.dankito.deepthought.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_article_summary.*
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ui.BaseActivity
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
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
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

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

    private var lastLoadedSummary: ArticleSummary? = null

    private val adapter = ArticleSummaryAdapter()

    private val selectedArticlesInContextualActionMode = LinkedHashSet<ArticleSummaryItem>()

    private var mnLoadMore: MenuItem? = null


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryPresenter(entryPersister, readLaterArticleService, tagService, searchEngine, router, dialogService)
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

            showArticleSummary(summary, false) // TODO: this is wrong as it only shows last loaded items but not all loaded items
        }
        else {
            extractArticlesSummary()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString(EXTRACTOR_URL_INTENT_EXTRA_NAME, extractorConfig?.url)

        outState?.putString(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, null) // fallback
        lastLoadedSummary?.let { outState?.putString(LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, serializer.serializeObject(it)) } // if not null
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
            icon.recycle()

            runOnUiThread {
                supportActionBar?.setIcon(BitmapDrawable(resources, scaledIcon))
            }
        } catch(e: Exception) {
            log.error("Could not load icon from url " + config.iconUrl, e)
        }
    }

    private fun extractArticlesSummary() {
        presenter.extractArticlesSummary(extractorConfig) {
            articleSummaryReceived(it, false)
        }
    }

    private fun loadMoreItems() {
        presenter.loadMoreItems(extractorConfig) {
            articleSummaryReceived(it, true)
        }
    }

    private fun articleSummaryReceived(result: AsyncResult<out ArticleSummary>, hasLoadedMoreItems: Boolean) {
        result.result?.let { showArticleSummaryThreadSafe(it, hasLoadedMoreItems)  }
        result.error?.let { /* TODO: show error */ }
    }

    private fun showArticleSummaryThreadSafe(summary: ArticleSummary, hasLoadedMoreItems: Boolean) {
        runOnUiThread { showArticleSummary(summary, hasLoadedMoreItems) }
    }

    private fun showArticleSummary(summary: ArticleSummary, hasLoadedMoreItems: Boolean) {
        this.lastLoadedSummary = summary

        mnLoadMore?.isVisible = summary.canLoadMoreItems

        if(hasLoadedMoreItems) {
            adapter.moreItemsHaveBeenLoaded(summary)
        }
        else {
            adapter.setArticleSummary(summary)
        }
    }

    private fun articleClicked(item: ArticleSummaryItem) {
        presenter.getAndShowArticle(item) {
            showArticleExtractionError(item, it)
        }
    }

    private fun showArticleExtractionError(item: ArticleSummaryItem, extractionError: Exception) {
        val errorMessage = getString(R.string.error_could_not_extract_article_from_url, item.url, extractionError.localizedMessage)

        runOnUiThread {
            val builder = AlertDialog.Builder(this)

            builder.setMessage(errorMessage)

            builder.setNegativeButton(android.R.string.ok, null)

            builder.create().show()
        }
    }


    private val lstArticleSummaryItemsMultiChoiceListener = object: AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.activity_article_summary_contextual_action_menu, menu)

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

            mode.title = getString(R.string.activity_article_summary_menu_count_articles_selected, selectedArticlesInContextualActionMode.size)
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.mnViewArticle -> {
                    showSelectedArticles()
                    mode.finish()
                    return true
                }
                R.id.mnSaveArticleForLaterReading -> {
                    saveSelectedArticlesForLaterReading()
                    mode.finish()
                    return true
                }
                R.id.mnSaveArticle -> {
                    saveSelectedArticles()
                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedArticlesInContextualActionMode.clear()
        }

    }

    private fun showSelectedArticles(): Boolean {
        selectedArticlesInContextualActionMode.forEach { presenter.getAndShowArticle(it) {
            // TODO: show error message
        } }

        return true
    }

    private fun saveSelectedArticlesForLaterReading(): Boolean {
        selectedArticlesInContextualActionMode.forEach { presenter.getAndSaveArticleForLaterReading(it) {
            // TODO: show error message
        } }

        return true
    }

    private fun saveSelectedArticles(): Boolean {
        selectedArticlesInContextualActionMode.forEach { presenter.getAndSaveArticle(it) {
            // TODO: show error message
        } }

        return true
    }

}
