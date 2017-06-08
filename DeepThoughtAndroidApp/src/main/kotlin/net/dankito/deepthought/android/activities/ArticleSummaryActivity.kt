package net.dankito.deepthought.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_article_summary.*
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryAdapter
import net.dankito.deepthought.android.routing.Router
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.Article
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.serializer.ISerializer
import net.dankito.serializer.JacksonJsonSerializer
import net.dankito.utils.ImageCache
import org.slf4j.LoggerFactory
import java.io.File

class ArticleSummaryActivity : AppCompatActivity() {

    companion object {
        const val EXTRACTOR_URL_INTENT_EXTRA_NAME = "EXTRACTOR_URL"
        const val LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME = "LAST_LOADED_SUMMARY"

        private val log = LoggerFactory.getLogger(ArticleSummaryActivity::class.java)
    }


    private lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager // TODO: inject

    private val webClient = OkHttpWebClient() // TODO: inject

    private val articleExtractors = ArticleExtractors(webClient) // TODO: inject

    private val serializer: ISerializer = JacksonJsonSerializer() // TODO: inject

    private var extractorConfig: ArticleSummaryExtractorConfig? = null

    private lateinit var imageCache: ImageCache

    private val adapter = ArticleSummaryAdapter()

    private var lastLoadedSummary: ArticleSummary? = null

    private lateinit var router: Router // TODO: inject

    private var mnLoadMore: MenuItem? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        router = Router(this.applicationContext)

        val fileStorageService = AndroidFileStorageService(this.applicationContext)
        extractorsConfigManager = ArticleSummaryExtractorConfigManager(webClient, fileStorageService)
        imageCache = ImageCache(webClient, serializer, fileStorageService)

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

    private fun restoreState(extractorClass: String?, serializedLastLoadedSummary: String?) {
        extractorClass?.let { initializeArticlesSummaryExtractor(it) }

        if(serializedLastLoadedSummary != null) {
            val summary = serializer.deserializeObject(serializedLastLoadedSummary, ArticleSummary::class.java)
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


    private fun initializeArticlesSummaryExtractor(extractorClassName: String) {
        try {
            extractorsConfigManager.getConfig(extractorClassName)?.let { config ->
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
        extractorConfig?.extractor?.extractSummaryAsync {
            it.result?.let { showArticleSummaryThreadSafe(it, false) }
        }
    }

    private fun loadMoreItems() {
        extractorConfig?.extractor?.loadMoreItemsAsync {
            it.result?.let { showArticleSummaryThreadSafe(it, true)  }
        }
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
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { showArticle(it) }
                asyncResult.error?.let { showArticleExtractionError(item, it) }
            }
        }
    }

    private fun showArticle(article: Article) {
        router.showArticleView(article)
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

}
