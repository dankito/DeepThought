package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_view_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ui.BaseActivity
import net.dankito.deepthought.android.views.EntryFieldsPreview
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ViewEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.serializer.ISerializer
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class ViewEntryActivity : BaseActivity() {

    companion object {
        const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var serializer: ISerializer


    private var entry: Entry? = null

    private var readLaterArticle: ReadLaterArticle? = null

    private var entryExtractionResult: EntryExtractionResult? = null


    private var presenter: ViewEntryPresenter

    private lateinit var entryFieldsPreview: EntryFieldsPreview


    init {
        AppComponent.component.inject(this)

        presenter = ViewEntryPresenter(entryPersister, clipboardService, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> showReadLaterArticleFromDatabase(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> showEntryFromDatabase(entryId) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let { outState ->
            outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, null)
            entry?.id?.let { entryId -> outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, entryId) }

            outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, null)
            readLaterArticle?.id?.let { readLaterArticleId -> outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticleId) }

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

        this.entryFieldsPreview = lytEntryFieldsPreview
        entryFieldsPreview.fieldClickedListener = { field -> editEntry()}

        val settings = wbEntry.getSettings()
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.defaultFontSize = 18 // default font is too small
        settings.javaScriptEnabled = true // so that embedded videos etc. work
    }


    override fun onResume() {
        super.onResume()

        intent.getStringExtra(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { showSerializedEntryExtractionResult(it) }
        intent.getStringExtra(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> showReadLaterArticleFromDatabase(readLaterArticleId) }
        intent.getStringExtra(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> showEntryFromDatabase(entryId) }
    }

    override fun onDestroy() {
        pauseWebView()

        super.onDestroy()
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

        menu?.findItem(R.id.mnSaveEntry)?.isVisible = (readLaterArticle != null || entryExtractionResult != null)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> presenter.returnToPreviousView()

            R.id.mnShareEntry -> showShareEntryPopupMenu(findViewById(R.id.mnShareEntry))

            R.id.mnEditEntry -> editEntry()

            R.id.mnSaveEntry -> {
                saveEntry()
            }
        }

        return true
    }

    private fun saveEntry() {
        entryExtractionResult?.let {
            presenter.saveEntryExtractionResult(it)
        }

        readLaterArticle?.let {
            presenter.saveEntryExtractionResult(it.entryExtractionResult)
            readLaterArticleService.delete(it)
        }
    }

    private fun editEntry() {
        entry?.let { presenter.editEntry(it) }

        readLaterArticle?.let { presenter.editEntry(it) }

        entryExtractionResult?.let { presenter.editEntry(it) }
    }

    private fun showShareEntryPopupMenu(clickedView: View) {
        val popup = PopupMenu(this, clickedView)

        popup.getMenuInflater().inflate(R.menu.share_entry_menu, popup.getMenu())

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem): Boolean {
                when(item.itemId) {
                    R.id.mnShareEntryReferenceUrl -> shareReferenceUrl()
                    R.id.mnShareEntryContent -> shareEntryContent()
                }
                return true
            }
        })

        popup.show()
    }

    private fun shareReferenceUrl() {
        entry?.reference?.let { reference ->
            presenter.shareReferenceUrl(reference)
        }

        entryExtractionResult?.reference?.let { reference ->
            presenter.shareReferenceUrl(reference)
        }
    }

    private fun shareEntryContent() {
        entry?.let { entry ->
            presenter.shareEntry(entry, entry.reference)
        }

        entryExtractionResult?.let { extractionResult ->
            presenter.shareEntry(extractionResult.entry, extractionResult.reference)
        }
    }


    private fun showEntryFromDatabase(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            this.entry = entry
            entryFieldsPreview.entry = entry

            showEntry(entry, entry.reference, entry.tags)
        }
    }

    private fun showReadLaterArticleFromDatabase(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            this.readLaterArticle = readLaterArticle
            entryFieldsPreview.readLaterArticle = readLaterArticle

            showEntry(readLaterArticle.entryExtractionResult.entry, readLaterArticle.entryExtractionResult.reference, readLaterArticle.entryExtractionResult.tags)
        }
    }

    private fun showSerializedEntryExtractionResult(serializedExtractionResult: String) {
        this.entryExtractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)
        entryFieldsPreview.entryExtractionResult = this.entryExtractionResult

        showEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags)
    }

    private fun showEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?) {
        val content = entry?.content
        val url = reference?.url

        if(url != null) {
            wbEntry.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", null, null)
        }
        else {
            wbEntry.loadData(content, "text/html; charset=UTF-8", null)
        }

        tags?.let { entryFieldsPreview.tagsOnEntry = it }

        entryFieldsPreview.setAbstractPreviewOnUIThread()
        entryFieldsPreview.setReferencePreviewOnUIThread()
        entryFieldsPreview.setTagsOnEntryPreviewOnUIThread()
    }

}
