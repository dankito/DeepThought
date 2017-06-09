package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_view_article.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.newsreader.model.Article
import net.dankito.serializer.ISerializer
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import javax.inject.Inject


class ViewArticleActivity : AppCompatActivity() {

    companion object {
        const val ARTICLE_INTENT_EXTRA_NAME = "ARTICLE"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var serializer: ISerializer

    private var article: Article? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        intent.getStringExtra(ARTICLE_INTENT_EXTRA_NAME)?.let { showSerializedArticle(it) }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ARTICLE_INTENT_EXTRA_NAME)?.let { showSerializedArticle(it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let { outState ->
            outState.putString(ARTICLE_INTENT_EXTRA_NAME, null) // fallback
            article?.let { outState.putString(ARTICLE_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_view_article)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = ""

        val settings = wbArticle.getSettings()
        settings.defaultTextEncodingName = "UTF-8"
        settings.javaScriptEnabled = true
    }


    override fun onPause() {
        pauseWebView()

        super.onPause()
    }

    private fun pauseWebView() {
        // to prevent that a video keeps on playing in WebView when navigating away from ViewArticleActivity
        // see https://stackoverflow.com/a/6230902
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause")
                    .invoke(wbArticle)

        } catch(ignored: Exception) { }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_article_activity_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                returnToPreviousView()
                return true
            }

            R.id.mnSaveArticle -> {
                saveArticle()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSerializedArticle(serializedArticle: String) {
        this.article = serializer.deserializeObject(serializedArticle, Article::class.java)

        wbArticle.loadDataWithBaseURL(article?.url, article?.content, "text/html; charset=UTF-8", null, null)
    }

    private fun saveArticle() {
        article?.let { article ->
            val entry = Entry(article.content, article.abstract ?: "")

            entry.reference = createAndPersistRefernce(article)

            entryService.persist(entry)
        }

        returnToPreviousView()
    }

    private fun createAndPersistRefernce(article: Article): Reference {
        val reference = Reference(article.title)

        reference.onlineAddress = article.url
        reference.publishingDate = article.publishingDate

        referenceService.persist(reference)

        return reference
    }


    private fun returnToPreviousView() {
        onBackPressed()
    }

}
