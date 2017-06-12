package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_view_article.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Reference
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import javax.inject.Inject


class ViewArticleActivity : AppCompatActivity() {

    companion object {
        const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var serializer: ISerializer

    private var entryExtractionResult: EntryExtractionResult? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        intent.getStringExtra(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let { outState ->
            outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, null) // fallback
            entryExtractionResult?.let { outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }
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

    private fun showSerializedEntryExtractionResult(serializedExtractionResult: String) {
        this.entryExtractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)

        wbArticle.loadDataWithBaseURL(entryExtractionResult?.reference?.onlineAddress, entryExtractionResult?.entry?.content, "text/html; charset=UTF-8", null, null)
    }

    private fun saveArticle() {
        entryExtractionResult?.let { entryExtractionResult ->
            val entry = entryExtractionResult.entry

            entry.reference = createAndPersistReference(entryExtractionResult)

            entryService.persist(entry)
        }

        returnToPreviousView()
    }

    private fun createAndPersistReference(entryExtractionResult: EntryExtractionResult): Reference? {
        val reference = entryExtractionResult.reference

        if(reference != null) {
            referenceService.persist(reference)
        }

        return reference
    }


    private fun returnToPreviousView() {
        onBackPressed()
    }

}
