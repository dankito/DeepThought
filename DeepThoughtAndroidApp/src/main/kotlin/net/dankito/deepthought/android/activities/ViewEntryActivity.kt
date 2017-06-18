package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_view_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ui.BaseActivity
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntryViewPresenter
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.serializer.ISerializer
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import javax.inject.Inject


class ViewEntryActivity : BaseActivity() {

    companion object {
        const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var serializer: ISerializer

    private var entry: Entry? = null

    private var entryExtractionResult: EntryExtractionResult? = null

    private lateinit var presenter: EntryViewPresenter


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        intent.getStringExtra(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
        intent.getStringExtra(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> showEntryFromDatabase(entryId) }

        presenter = EntryViewPresenter(entry, entryExtractionResult, entryService, referenceService, router)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> showEntryFromDatabase(entryId) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let { outState ->
            outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, null)
            entry?.id?.let { entryId -> outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, entryId) }

            outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, null)
            entryExtractionResult?.let { outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_view_entry)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = ""

        val settings = wbEntry.getSettings()
        settings.defaultTextEncodingName = "UTF-8"
        settings.javaScriptEnabled = true
    }


    override fun onPause() {
        pauseWebView()

        super.onPause()
    }

    private fun pauseWebView() {
        // to prevent that a video keeps on playing in WebView when navigating away from ViewEntryActivity
        // see https://stackoverflow.com/a/6230902
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause")
                    .invoke(wbEntry)

        } catch(ignored: Exception) { }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_entry_activity_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                presenter.returnToPreviousView()
                return true
            }

            R.id.mnSaveEntry -> {
                presenter.saveEntry()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun showEntryFromDatabase(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            showEntry(entry)
        }
    }

    private fun showEntry(entry: Entry) {
        val url = entry.reference?.onlineAddress

        if(url != null) {
            wbEntry.loadDataWithBaseURL(url, entry.content, "text/html; charset=UTF-8", null, null)
        }
        else {
            wbEntry.loadData(entry.content, "text/html; charset=UTF-8", null)
        }
    }

    private fun showSerializedEntryExtractionResult(serializedExtractionResult: String) {
        this.entryExtractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)

        wbEntry.loadDataWithBaseURL(entryExtractionResult?.reference?.onlineAddress, entryExtractionResult?.entry?.content, "text/html; charset=UTF-8", null, null)
    }

}
