package net.dankito.deepthought.android.activities

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.PopupMenu
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.activities.arguments.EntryActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.EditHtmlTextDialog
import net.dankito.deepthought.android.dialogs.TagsOnEntryDialogFragment
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.views.ActionItemHelper
import net.dankito.deepthought.android.views.FullScreenWebView
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import java.util.*
import javax.inject.Inject


class EditEntryActivity : BaseActivity() {

    companion object {
        private const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        private const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val ABSTRACT_INTENT_EXTRA_NAME = "ABSTRACT"
        private const val TAGS_ON_ENTRY_INTENT_EXTRA_NAME = "TAGS_ON_ENTRY"

        const val ResultId = "EDIT_ENTRY_ACTIVITY_RESULT"

        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"

        private const val NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS = 0
        private val FULLSCREEN_MODE_SYSTEM_UI_FLAGS: Int


        init {
            FULLSCREEN_MODE_SYSTEM_UI_FLAGS = createFullscreenModeSystemUiFlags()
        }

        private fun createFullscreenModeSystemUiFlags(): Int {
            // see https://developer.android.com/training/system-ui/immersive.html
            var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                // even thought View.SYSTEM_UI_FLAG_FULLSCREEN is also available from SDK 16 and above, to my experience it doesn't work reliable (at least not on Android 4.1)
                flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
            }

            return flags
        }
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var threadPool: IThreadPool

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var eventBus: IEventBus


    private var entry: Entry? = null

    private var readLaterArticle: ReadLaterArticle? = null

    private var entryExtractionResult: EntryExtractionResult? = null


    private var contentToEdit: String? = null

    private var abstractToEdit: String? = null

    private var referenceToEdit: Reference? = null

    private val tagsOnEntry: MutableList<Tag> = ArrayList()

    private var canEntryBeSaved = false

    private var entryHasBeenEdited = false


    private val presenter: EditEntryPresenter

    private var isInFullscreenMode = false

    private var isInReaderMode = false

    private lateinit var swipeTouchListener: OnSwipeTouchListener


    private val actionItemHelper = ActionItemHelper()

    private var mnSaveEntry: MenuItem? = null

    private var mnToggleReaderMode: MenuItem? = null

    private var mnSaveEntryExtractionResultForLaterReading: MenuItem? = null

    private var mnDeleteReadLaterArticle: MenuItem? = null


    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, readLaterArticleService, clipboardService, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditEntryActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EntryActivityParameters)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { editEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }

        savedInstanceState.getString(CONTENT_INTENT_EXTRA_NAME)?.let { content ->
            contentToEdit = content
            setContentPreviewOnUIThread()
        }

        savedInstanceState.getString(ABSTRACT_INTENT_EXTRA_NAME)?.let { abstract ->
            abstractToEdit = abstract
            setAbstractPreviewOnUIThread()
        }

        savedInstanceState.getString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME)?.let { tagsOnEntryIds -> restoreTagsOnEntryAsync(tagsOnEntryIds) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, null)
            entry?.id?.let { entryId -> outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, entryId) }

            outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, null)
            readLaterArticle?.id?.let { readLaterArticleId -> outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticleId) }

            outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, null)
            entryExtractionResult?.let { outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }

            outState.putString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME, serializer.serializeObject(tagsOnEntry))

            outState.putString(CONTENT_INTENT_EXTRA_NAME, contentToEdit)

            outState.putString(ABSTRACT_INTENT_EXTRA_NAME, abstractToEdit)
        }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_entry)

        setSupportActionBar(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }

        lytAbstractPreview.setOnClickListener { editAbstract() }
        lytReferencePreview.setOnClickListener { editReference() }
        btnClearEntryReference.setOnClickListener { referenceCleared() }
        lytTagsPreview.setOnClickListener { editTagsOnEntry() }

        setupEntryContentView()
    }

    private fun referenceCleared() {
        referenceToEdit = null

        entryHasBeenEdited()

        updateCanEntryBeSavedOnUIThread(true)
        setReferencePreviewOnUIThread()
    }

    private fun setupEntryContentView() {
        wbEntry.setOnSystemUiVisibilityChangeListener { flags -> systemUiVisibilityChanged(flags) }
        wbEntry.changeFullScreenModeListener = { mode -> handleChangeFullScreenModeEvent(mode) }

        swipeTouchListener = OnSwipeTouchListener(this) { handleWebViewSwipe(it) }
        swipeTouchListener.singleTapListener = { handleWebViewClick() }
        swipeTouchListener.doubleTapListener = { handleWebViewDoubleTap() }

        wbEntry.setOnTouchListener { _, event -> handleWebViewTouch(event) }

        val settings = wbEntry.getSettings()
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.defaultFontSize = 18 // default font is too small
        settings.domStorageEnabled = true // otherwise images may not load, see https://stackoverflow.com/questions/29888395/images-not-loading-in-android-webview
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptEnabled = true // so that embedded videos etc. work

        wbEntry.addJavascriptInterface(GetHtmlCodeFromWebViewJavaScripInterface { url, html -> siteFinishedLoading(url, html) }, GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        wbEntry.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(webView: WebView, url: String?) {
                super.onPageFinished(webView, url)

                // if EntryExtractionResult's entry content hasn't been extracted yet, wait till WebView is loaded and extract entry content then
                if(entryExtractionResult?.couldExtractContent == false) {
                    webView.loadUrl("javascript:$GetHtmlCodeFromWebViewJavaScriptInterfaceName.finishedLoadingSite" +
                            "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                }
            }
        })
    }

    private fun siteFinishedLoading(url: String, html: String) {
        entryExtractionResult?.webSiteHtml = html

        // now try to extract entry content from WebView's html
        if(entryExtractionResult?.couldExtractContent == false) {
            contentToEdit = html

            entryExtractionResult?.let { extractionResult ->
                articleExtractorManager.extractArticleAndAddDefaultDataAsync(extractionResult, html, url)
                if(extractionResult.couldExtractContent) {
                    runOnUiThread { extractedContentOnUiThread(extractionResult) }
                }
            }
        }
    }

    private fun extractedContentOnUiThread(extractionResult: EntryExtractionResult) {
        wbEntry.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        mnToggleReaderMode?.isVisible = extractionResult.couldExtractContent
    }


    inner class GetHtmlCodeFromWebViewJavaScripInterface(private val retrievedHtmlCode: ((url: String, html: String) -> Unit)) {

        @JavascriptInterface
        @SuppressWarnings("unused")
        fun finishedLoadingSite(url: String, html: String) {
            retrievedHtmlCode(url, html)
        }

    }


    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditReferenceActivity.ResultId) as? EditReferenceActivityResult)?.let { result ->
            if(result.didSaveReference) {
                result.savedReference?.let { savedReference(it) }
            }
        }

        setOnboardingTextVisibilityOnUIThread()
    }

    private fun setOnboardingTextVisibilityOnUIThread() {
        if(contentToEdit.isNullOrBlank() && (entryExtractionResult == null || entryExtractionResult?.couldExtractContent == true)) {
            wbEntry.visibility = View.GONE
            txtOnboardingText.visibility = View.VISIBLE

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(R.string.activity_edit_entry_edit_content_onboarding_text).toString(), Html.FROM_HTML_MODE_LEGACY)
            }
            else {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(R.string.activity_edit_entry_edit_content_onboarding_text).toString())
            }

            txtOnboardingText.setOnClickListener { editContent() }
        }
        else {
            wbEntry.visibility = View.VISIBLE
            txtOnboardingText.visibility = View.GONE
        }
    }

    private fun savedReference(reference: Reference) {
        referenceToEdit = reference // do not set reference directly on entry as if entry is not saved yet adding it to reference.entries causes an error

        updateCanEntryBeSavedOnUIThread(true)
        setReferencePreviewOnUIThread()
    }


    private fun editContent() {
        contentToEdit?.let { content ->
            val editHtmlTextDialog = EditHtmlTextDialog()

            editHtmlTextDialog.showDialog(supportFragmentManager, content, R.string.activity_edit_entry_edit_content_title) {
                contentToEdit = it

                entryHasBeenEdited()

                runOnUiThread {
                    updateCanEntryBeSavedOnUIThread(true)
                    setContentPreviewOnUIThread()
                }
            }
        }
    }

    private fun editAbstract() {
        abstractToEdit?.let { abstract ->
            val editHtmlTextDialog = EditHtmlTextDialog()

            editHtmlTextDialog.showDialog(supportFragmentManager, abstract, R.string.activity_edit_entry_edit_abstract_title) {
                abstractToEdit = it

                entryHasBeenEdited()

                runOnUiThread {
                    updateCanEntryBeSavedOnUIThread(true)
                    setAbstractPreviewOnUIThread()
                }
            }
        }
    }

    private fun editReference() {
        setWaitingForResult(EditReferenceActivity.ResultId)

        val reference = referenceToEdit
        val entry = entry ?: readLaterArticle?.entryExtractionResult?.entry ?: entryExtractionResult?.entry ?: Entry("") // should never be the case that entry is null, just to make compiler happy

        presenter.editReference(reference, entry)
    }

    private fun editTagsOnEntry() {
        val tagsOnEntryDialog = TagsOnEntryDialogFragment()

        tagsOnEntryDialog.show(supportFragmentManager, tagsOnEntry) {
            tagsOnEntry.clear()
            tagsOnEntry.addAll(it)

            entryHasBeenEdited()

            runOnUiThread {
                updateCanEntryBeSavedOnUIThread(true)
                setTagsOnEntryPreviewOnUIThread()
            }
        }
    }

    private fun updateCanEntryBeSavedOnUIThread(canEntryBeSaved: Boolean) {
        this.canEntryBeSaved = canEntryBeSaved

        setMenuSaveEntryVisibleStateOnUIThread()
    }

    private fun setMenuSaveEntryVisibleStateOnUIThread() {
        mnSaveEntry?.isVisible = canEntryBeSaved
    }


    private fun setContentPreviewOnUIThread() {
        setContentPreviewOnUIThread(referenceToEdit)
    }

    private fun setContentPreviewOnUIThread(reference: Reference?) {
        var content = contentToEdit
        val url = reference?.url

        if(content.isNullOrBlank() && entryExtractionResult != null && isInReaderMode == false && url != null) {
            wbEntry.loadUrl(url)
        }
        else {
            if(content?.startsWith("<html") == false && content?.startsWith("<body") == false) {
                content = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + content + "</body>"
            }

            if(url != null && Build.VERSION.SDK_INT > 16) {
                wbEntry.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", "utf-8", null)
            }
            else {
                wbEntry.loadData(content, "text/html; charset=UTF-8", null)
            }
        }

        setOnboardingTextVisibilityOnUIThread()
    }

    private fun setAbstractPreviewOnUIThread() {
        abstractToEdit?.let { lytAbstractPreview.setFieldOnUIThread(getString(R.string.activity_edit_entry_abstract_label), it.getPlainTextForHtml()) }
    }

    private fun setReferencePreviewOnUIThread() {
        referenceToEdit?.let { lytReferencePreview.setFieldOnUIThread(getString(R.string.activity_edit_entry_reference_label), it.previewWithSeriesAndPublishingDate) }

        if(referenceToEdit == null) {
            lytReferencePreview.setFieldOnUIThread(getString(R.string.activity_edit_entry_reference_label), "")
        }

        btnClearEntryReference.visibility = if(referenceToEdit == null) View.GONE else View.VISIBLE
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        val tagsPreview = tagsOnEntry.filterNotNull().sortedBy { it.name.toLowerCase() }.joinToString { it.name }
        lytTagsPreview.setFieldOnUIThread(getString(R.string.activity_edit_entry_tags_label), tagsPreview)
    }


    private fun systemUiVisibilityChanged(flags: Int) {
        // as immersive fullscreen is only available for KitKat and above leave immersive fullscreen mode by swiping from screen top or bottom is also only available on these  devices
        if(flags == NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            leaveFullscreenMode()
        }
    }

    /**
     * WebView doesn't fire click event, so we had to implement this our self
     */
    private fun handleWebViewTouch(event: MotionEvent): Boolean {
        swipeTouchListener.onTouch(wbEntry, event)

        return false // don't consume event as otherwise scrolling won't work anymore
    }

    private fun handleWebViewClick() {
        val hitResult = wbEntry.hitTestResult
        val type = hitResult.type

        // leave the functionality for clicking on links, phone numbers, geo coordinates, ... Only go to fullscreen mode when clicked somewhere else in the WebView or on an image
        if(type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.IMAGE_TYPE) {
            if(isInFullscreenMode) {
                leaveFullscreenMode()
            }
            else {
                editContent()
            }
        }
    }

    private fun handleChangeFullScreenModeEvent(mode: FullScreenWebView.FullScreenMode) {
        when(mode) {
            FullScreenWebView.FullScreenMode.Enter -> enterFullscreenMode()
            FullScreenWebView.FullScreenMode.Leave -> leaveFullscreenMode()
        }
    }

    private fun handleWebViewDoubleTap() {
        if(isInFullscreenMode) {
            saveEntryAndCloseDialog()
        }
    }

    private fun handleWebViewSwipe(swipeDirection: OnSwipeTouchListener.SwipeDirection) {
        if(isInFullscreenMode) {
            when(swipeDirection) {
                OnSwipeTouchListener.SwipeDirection.Left -> presenter.returnToPreviousView()
                OnSwipeTouchListener.SwipeDirection.Right -> editTagsOnEntry()
            }
        }
    }

    private fun leaveFullscreenMode() {
        isInFullscreenMode = false

        lytAbstractPreview.visibility = View.VISIBLE
        lytReferencePreview.visibility = View.VISIBLE
        lytTagsPreview.visibility = View.VISIBLE
        txtEntryContentLabel.visibility = View.VISIBLE
        appBarLayout.visibility = View.VISIBLE

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = false
        wbEntry.layoutParams = layoutParams

        wbEntry.systemUiVisibility = NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS
    }

    private fun enterFullscreenMode() {
        isInFullscreenMode = true

        lytAbstractPreview.visibility = View.GONE
        lytReferencePreview.visibility = View.GONE
        lytTagsPreview.visibility = View.GONE
        txtEntryContentLabel.visibility = View.GONE
        appBarLayout.visibility = View.GONE

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = true
        wbEntry.layoutParams = layoutParams

        wbEntry.systemUiVisibility = FULLSCREEN_MODE_SYSTEM_UI_FLAGS
    }


    override fun onDestroy() {
        pauseWebView()

        parameterHolder.clearActivityResults(EditReferenceActivity.ResultId)

        unregisterEventBusListener()

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

    override fun onBackPressed() {
        if(isEditEntryFieldDialogVisible()) { // let TagEntriesListDialog handle back button press
            super.onBackPressed()
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    private fun isEditEntryFieldDialogVisible(): Boolean {
        return supportFragmentManager.findFragmentByTag(EditHtmlTextDialog.TAG) != null || supportFragmentManager.findFragmentByTag(TagsOnEntryDialogFragment.TAG) != null
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_entry_menu, menu)
        actionItemHelper.setupLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        mnSaveEntry = menu.findItem(R.id.mnSaveEntry)

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        mnToggleReaderMode?.isVisible = entryExtractionResult?.couldExtractContent ?: false

        mnSaveEntryExtractionResultForLaterReading = menu.findItem(R.id.mnSaveEntryExtractionResultForLaterReading)
        mnSaveEntryExtractionResultForLaterReading?.isVisible = entryExtractionResult != null

        mnDeleteReadLaterArticle = menu.findItem(R.id.mnDeleteReadLaterArticle)
        mnDeleteReadLaterArticle?.isVisible = readLaterArticle != null

        setMenuSaveEntryVisibleStateOnUIThread()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveEntry -> {
                saveEntryAndCloseDialog()
                return true
            }
            R.id.mnToggleReaderMode -> {
                toggleReaderMode()
                return true
            }
            R.id.mnSaveEntryExtractionResultForLaterReading -> {
                saveEntryExtrationResultForLaterReadingAndCloseDialog()
                return true
            }
            R.id.mnDeleteReadLaterArticle -> {
                deleteReadLaterArticleAndCloseDialog()
                return true
            }
            R.id.mnShareEntry -> {
                showShareEntryPopupMenu()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun toggleReaderMode() {
        isInReaderMode = !isInReaderMode

        if(isInReaderMode) {
            contentToEdit = entryExtractionResult?.entry?.content ?: ""
        }
        else {
            contentToEdit = entryExtractionResult?.webSiteHtml ?: ""
        }

        setContentPreviewOnUIThread()
    }

    private fun showShareEntryPopupMenu() {
        val overflowMenuButton = getOverflowMenuButton()
        if(overflowMenuButton == null) {
            return
        }

        val popup = PopupMenu(this, overflowMenuButton)

        popup.menuInflater.inflate(R.menu.share_entry_menu, popup.menu)

        val reference = referenceToEdit
        if(reference == null || reference.url.isNullOrBlank()) {
            popup.menu.findItem(R.id.mnShareEntryReferenceUrl).isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.mnShareEntryReferenceUrl -> shareReferenceUrl()
                R.id.mnShareEntryContent -> shareEntryContent()
            }
            true
        }

        popup.show()
    }

    private fun getOverflowMenuButton(): View? {
        for(i in 0..toolbar.childCount - 1) { // OverflowMenuButton is child of ActionMenuView which is child of toolbar (both don't have an id so i cannot search for them)
            val child = toolbar.getChildAt(i)

            if(child is ActionMenuView) {
                for(j in 0..child.childCount) {
                    val actionMenuViewChild = child.getChildAt(j)

                    if(actionMenuViewChild is AppCompatImageView && actionMenuViewChild is ActionMenuView.ActionMenuChildView) {
                        return actionMenuViewChild
                    }
                }
            }
        }

        return null
    }

    private fun shareReferenceUrl() {
        referenceToEdit?.let { reference ->
            presenter.shareReferenceUrl(reference)
        }
    }

    private fun shareEntryContent() {
        entry?.let { entry ->
            presenter.shareEntry(entry, entry.reference)
        }

        readLaterArticle?.entryExtractionResult?.let {  extractionResult ->
            presenter.shareEntry(extractionResult.entry, extractionResult.reference)
        }

        entryExtractionResult?.let { extractionResult ->
            presenter.shareEntry(extractionResult.entry, extractionResult.reference)
        }
    }


    private fun saveEntryAndCloseDialog() {
        mnSaveEntry?.isEnabled = false // disable to that save cannot be pressed a second time
        mnSaveEntryExtractionResultForLaterReading?.isEnabled = false
        unregisterEventBusListener()

        saveEntryAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mnSaveEntry?.isEnabled = true
                mnSaveEntryExtractionResultForLaterReading?.isEnabled = true
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveEntryAsync(callback: (Boolean) -> Unit) {
        var content = contentToEdit ?: ""
        val abstract = abstractToEdit ?: ""

        entry?.let { entry ->
            updateEntry(entry, content, abstract)
            presenter.saveEntryAsync(entry, referenceToEdit, tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntry = true, savedEntry = entry))
                }
                callback(successful)
            }
        }

        entryExtractionResult?.let { extractionResult ->
            if(extractionResult.couldExtractContent == false) {
                extractionResult.webSiteHtml?.let { content = it }
            }
            if(extractionResult.couldExtractContent && extractionResult.entry.content.isNullOrBlank() == false) {
                content = extractionResult.entry.content
            }

            updateEntry(extractionResult.entry, content, abstract)
            presenter.saveEntryAsync(extractionResult.entry, referenceToEdit, tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedEntry = extractionResult.entry))
                }
                callback(successful)
            }
        }

        readLaterArticle?.let { readLaterArticle ->
            val extractionResult = readLaterArticle.entryExtractionResult
            updateEntry(extractionResult.entry, content, abstract)

            presenter.saveEntryAsync(extractionResult.entry, referenceToEdit, tagsOnEntry) { successful ->
                if(successful) {
                    readLaterArticleService.delete(readLaterArticle)
                    setActivityResult(EditEntryActivityResult(didSaveReadLaterArticle = true, savedEntry = extractionResult.entry))
                }
                callback(successful)
            }
        }
    }

    private fun saveEntryExtrationResultForLaterReadingAndCloseDialog() {
        mnSaveEntry?.isEnabled = false // disable to that save cannot be pressed a second time
        mnSaveEntryExtractionResultForLaterReading?.isEnabled = false
        unregisterEventBusListener()

        saveEntryForLaterReading { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mnSaveEntry?.isEnabled = true
                mnSaveEntryExtractionResultForLaterReading?.isEnabled = true
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveEntryForLaterReading(callback: (Boolean) -> Unit) {
        val content = contentToEdit ?: ""
        val abstract = abstractToEdit ?: ""

        entryExtractionResult?.let { extractionResult ->
            updateEntry(extractionResult.entry, content, abstract)
            extractionResult.reference = referenceToEdit
            extractionResult.tags = tagsOnEntry

            presenter.saveEntryExtractionResultForLaterReading(extractionResult)
            setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedEntry = extractionResult.entry))
            callback(true)
        }

        if(entryExtractionResult == null) {
            callback(false)
        }
    }

    private fun deleteReadLaterArticleAndCloseDialog() {
        readLaterArticle?.let { readLaterArticle ->
            mnSaveEntry?.isEnabled = false // disable to that save cannot be pressed a second time
            mnDeleteReadLaterArticle?.isEnabled = false
            unregisterEventBusListener()

            presenter.deleteReadLaterArticle(readLaterArticle)

            runOnUiThread { closeDialog() }
        }
    }

    private fun setActivityResult(result: EditEntryActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    private fun updateEntry(entry: Entry, content: String, abstract: String) {
        entry.content = content
        entry.abstractString = abstract
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(entryHasBeenEdited) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_entry_alert_message_entry_contains_unsaved_changes)) { shouldChangedGetSaved ->
            runOnUiThread {
                if(shouldChangedGetSaved) {
                    saveEntryAndCloseDialog()
                }
                else {
                    closeDialog()
                }
            }
        }
    }

    private fun closeDialog() {
        finish()
    }


    private fun showParameters(parameters: EntryActivityParameters?) {
        if(parameters == null) { // create entry
            if(entry == null) { // entry != null -> entry has been restored from savedInstanceState, parameters therefor is null
                createEntry()
            }
        }
        else {
            parameters.entry?.let { editEntry(it) }

            parameters.readLaterArticle?.let { editReadLaterArticle(it) }

            parameters.entryExtractionResult?.let { editEntryExtractionResult(it) }
        }
    }

    private fun createEntry() {
        canEntryBeSaved = true

        editEntry(Entry(""))
        editContent() // go directly to edit content dialog, there's absolutely nothing to see on this almost empty screen
    }

    private fun editEntry(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            editEntry(entry)
        }
    }

    private fun editEntry(entry: Entry) {
        this.entry = entry

        editEntry(entry, entry.reference, entry.tags)
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            editReadLaterArticle(readLaterArticle)
        }
    }

    private fun editReadLaterArticle(readLaterArticle: ReadLaterArticle) {
        this.readLaterArticle = readLaterArticle
        canEntryBeSaved = true

        editEntry(readLaterArticle.entryExtractionResult.entry, readLaterArticle.entryExtractionResult.reference, readLaterArticle.entryExtractionResult.tags)
    }

    private fun editEntryExtractionResult(serializedExtractionResult: String) {
        val extractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)

        editEntryExtractionResult(extractionResult)
    }

    private fun editEntryExtractionResult(extractionResult: EntryExtractionResult) {
        this.entryExtractionResult = extractionResult
        canEntryBeSaved = true

        editEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags)
    }

    private fun editEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?) {
        contentToEdit = entry?.content
        abstractToEdit = entry?.abstractString
        referenceToEdit = reference

        setContentPreviewOnUIThread(reference)

        setAbstractPreviewOnUIThread()

        setReferencePreviewOnUIThread()

        tags?.let {
            tagsOnEntry.addAll(tags)

            setTagsOnEntryPreviewOnUIThread()
        }

        mayRegisterEventBusListener()
    }


    private fun restoreTagsOnEntryAsync(tagsOnEntryIdsString: String) {
        threadPool.runAsync { restoreTagsOnEntry(tagsOnEntryIdsString) }
    }

    private fun restoreTagsOnEntry(tagsOnEntryIdsString: String) {
        val restoredTagsOnEntry = serializer.deserializeObject(tagsOnEntryIdsString, List::class.java, Tag::class.java) as List<Tag>

        tagsOnEntry.clear()
        tagsOnEntry.addAll(restoredTagsOnEntry)

        runOnUiThread { setTagsOnEntryPreviewOnUIThread() }
    }


    private fun contentHasBeenEdited() {
        entryHasBeenEdited()
        runOnUiThread { updateCanEntryBeSavedOnUIThread(true) }
    }

    private fun entryHasBeenEdited() {
        entryHasBeenEdited = true
    }


    private fun mayRegisterEventBusListener() {
        if(entry?.isPersisted() ?: false) {
            synchronized(this) {
                val eventBusListenerInit = EventBusListener()

                eventBus.register(eventBusListenerInit)

                this.eventBusListener = eventBusListenerInit
            }
        }
    }

    private fun unregisterEventBusListener() {
        synchronized(this) {
            eventBusListener?.let {
                eventBus.unregister(it)
            }

            this.eventBusListener = null
        }
    }

    private fun entryHasBeenEdited(entry: Entry) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_entry_alert_message_entry_has_been_edited))
        }
    }

    inner class EventBusListener {

        @Handler
        fun entryChanged(change: EntryChanged) {
            if(change.entity == entry) {
                entryHasBeenEdited(change.entity)
            }
        }
    }

}
