package net.dankito.deepthought.android.activities

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_view_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.activities.arguments.EntryActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.views.EntryFieldsPreview
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ViewEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IClipboardService
import java.util.*
import javax.inject.Inject


class ViewEntryActivity : BaseActivity() {

    companion object {
        private const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        private const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        private const val MAX_CLICK_DURATION = 200
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

    private var isInReaderMode = false

    private var webViewClickStartTime: Long = 0

    private lateinit var entryFieldsPreview: EntryFieldsPreview


    init {
        AppComponent.component.inject(this)

        presenter = ViewEntryPresenter(entryPersister, clipboardService, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EntryActivityParameters)
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
        
        wbEntry.setOnTouchListener { _, event -> handleWebViewTouch(event) }
        wbEntry.setOnClickListener { handleWebViewClick() }

        this.entryFieldsPreview = lytEntryFieldsPreview
        entryFieldsPreview.fieldClickedListener = { field -> editEntry(field)}

        val settings = wbEntry.getSettings()
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.defaultFontSize = 18 // default font is too small
        settings.javaScriptEnabled = true // so that embedded videos etc. work
    }


    override fun onResume() {
        super.onResume()

        (parameterHolder.getActivityResult(EditEntryActivity.ResultId) as? EditEntryActivityResult)?.let { result ->
            if(result.didSaveReadLaterArticle) {
                this.readLaterArticle = null
            }
            if(result.didSaveEntryExtractionResult) {
                this.entryExtractionResult = null
            }

            result.savedEntry?.let { savedEntry ->
                showEntry(savedEntry)
            }
        }
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
                saveEntryAsync()
            }
        }

        return true
    }

    private fun saveEntryAsync() {
        entryExtractionResult?.let {
            presenter.saveEntryExtractionResultAsync(it)
        }

        readLaterArticle?.let { article ->
            presenter.saveEntryExtractionResultAsync(article.entryExtractionResult) { successful ->
                if(successful) {
                    readLaterArticleService.delete(article)
                }
            }
        }
    }

    private fun editEntry(field: EntryField? = null) {
        entry?.let { presenter.editEntry(it, field) }

        readLaterArticle?.let { presenter.editEntry(it, field) }

        entryExtractionResult?.let { presenter.editEntry(it, field) }
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


    /**
     * WebView doesn't fire click event, so we had to implement this our self
     */
    private fun handleWebViewTouch(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                webViewClickStartTime = Calendar.getInstance().timeInMillis
            }
            MotionEvent.ACTION_UP -> {
                val clickDuration = Calendar.getInstance().timeInMillis - webViewClickStartTime
                if(clickDuration < MAX_CLICK_DURATION) {
                    wbEntry.performClick()
                }
            }
        }

        return false
    }

    private fun handleWebViewClick() {
        val hitResult = wbEntry.hitTestResult
        val type = hitResult.type

        // leave the functionality for clicking on links, phone numbers, geo coordinates, ... Only go to reader mode when clicked somewhere else in the WebView or on an image
        if(type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.IMAGE_TYPE) {
            toggleReaderMode()
        }
    }

    private fun toggleReaderMode() {
        if(isInReaderMode) {
            isInReaderMode = false

            leaveReaderMode()
        }
        else {
            isInReaderMode = true

            goToReaderMode()
        }
    }

    private fun leaveReaderMode() {
        entryFieldsPreview.visibility = View.VISIBLE
        appBarLayout.visibility = View.VISIBLE

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = true
        wbEntry.layoutParams = layoutParams
    }

    private fun goToReaderMode() {
        entryFieldsPreview.visibility = View.GONE
        appBarLayout.visibility = View.GONE

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = false
        wbEntry.layoutParams = layoutParams
    }


    private fun showParameters(parameters: EntryActivityParameters?) {
        if(parameters != null) {
            parameters.entry?.let { showEntry(it) }

            parameters.readLaterArticle?.let { showReadLaterArticle(it) }

            parameters.entryExtractionResult?.let { showEntryExtractionResult(it) }
        }
    }

    private fun showEntryFromDatabase(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            showEntry(entry)
        }
    }

    private fun showEntry(entry: Entry) {
        this.entry = entry
        entryFieldsPreview.entry = entry

        showEntry(entry, entry.reference, entry.tags)
    }

    private fun showReadLaterArticleFromDatabase(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            showReadLaterArticle(readLaterArticle)
        }
    }

    private fun showReadLaterArticle(readLaterArticle: ReadLaterArticle) {
        this.readLaterArticle = readLaterArticle
        entryFieldsPreview.readLaterArticle = readLaterArticle

        showEntry(readLaterArticle.entryExtractionResult.entry, readLaterArticle.entryExtractionResult.reference, readLaterArticle.entryExtractionResult.tags)
    }

    private fun showSerializedEntryExtractionResult(serializedExtractionResult: String) {
        val entryExtractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)
        showEntryExtractionResult(entryExtractionResult)
    }

    private fun showEntryExtractionResult(extractionResult: EntryExtractionResult) {
        this.entryExtractionResult = extractionResult
        entryFieldsPreview.entryExtractionResult = this.entryExtractionResult

        showEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags)
    }

    private fun showEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?) {
        var content = entry?.content
        val url = reference?.url

        if(content?.startsWith("<html") == false && content?.startsWith("<body") == false) {
            content = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + content + "</body>"
        }

        if(url != null && Build.VERSION.SDK_INT > 16) {
            wbEntry.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", "utf-8", null)
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
