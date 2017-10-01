package net.dankito.deepthought.android.activities

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.PopupMenu
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_entry.*
import kotlinx.android.synthetic.main.view_floating_action_button_entry_fields.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.activities.arguments.EntryActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.EditHtmlTextDialog
import net.dankito.deepthought.android.dialogs.TagsOnEntryDialogFragment
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.views.ContextHelpUtil
import net.dankito.deepthought.android.views.FullscreenWebView
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.ReferenceService
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
import kotlin.concurrent.schedule


class EditEntryActivity : BaseActivity() {

    companion object {
        private const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        private const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        private const val FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_REFERENCE_PREVIEW"
        private const val FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_ABSTRACT_PREVIEW"

        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val ABSTRACT_INTENT_EXTRA_NAME = "ABSTRACT"
        private const val REFERENCE_INTENT_EXTRA_NAME = "REFERENCE"
        private const val TAGS_ON_ENTRY_INTENT_EXTRA_NAME = "TAGS_ON_ENTRY"

        const val ResultId = "EDIT_ENTRY_ACTIVITY_RESULT"

        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var referenceService: ReferenceService

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

    private var forceShowAbstractPreview = false

    private var forceShowReferencePreview = false

    private var canEntryBeSaved = false

    private var entryHasBeenEdited = false


    private val presenter: EditEntryPresenter

    private var isInReaderMode = false


    private val contextHelpUtil = ContextHelpUtil()

    private val toolbarUtil = ToolbarUtil()

    private var mnSaveEntry: MenuItem? = null

    private var mnToggleReaderMode: MenuItem? = null

    private var mnSaveEntryExtractionResultForLaterReading: MenuItem? = null

    private var mnDeleteReadLaterArticle: MenuItem? = null

    private var mnShareEntry: MenuItem? = null


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

        if(savedInstanceState == null) {
            showParameters(getParameters() as? EntryActivityParameters)
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        this.forceShowReferencePreview = savedInstanceState.getBoolean(FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowAbstractPreview = savedInstanceState.getBoolean(FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME, false)

        this.isInReaderMode = savedInstanceState.getBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, false)

        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { editEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }

        if(savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME) == null && savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME) == null &&
                savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME) == null) { // a new Entry is being created then
            createEntry(false) // don't go to EditHtmlTextDialog for content here as we're restoring state, content may already be set
        }

        savedInstanceState.getString(CONTENT_INTENT_EXTRA_NAME)?.let { content ->
            contentToEdit = content
            setContentPreviewOnUIThread()
        }

        savedInstanceState.getString(ABSTRACT_INTENT_EXTRA_NAME)?.let { abstract ->
            abstractToEdit = abstract
            setAbstractPreviewOnUIThread()
        }

        savedInstanceState.getString(REFERENCE_INTENT_EXTRA_NAME)?.let { referenceID -> restoreReference(referenceID) }

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

            outState.putBoolean(FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME, forceShowReferencePreview)
            outState.putBoolean(FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME, forceShowAbstractPreview)

            outState.putBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, isInReaderMode)

            outState.putString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME, serializer.serializeObject(tagsOnEntry))

            outState.putString(REFERENCE_INTENT_EXTRA_NAME, referenceToEdit?.id)

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

        lytAbstractPreview.setFieldNameOnUiThread(getString(R.string.activity_edit_entry_abstract_label))
        lytReferencePreview.setFieldNameOnUiThread(getString(R.string.activity_edit_entry_reference_label))
        lytTagsPreview.setFieldNameOnUiThread(getString(R.string.activity_edit_entry_tags_label))

        lytAbstractPreview.setOnClickListener { editAbstract() }
        lytReferencePreview.setOnClickListener { editReference() }
        btnClearEntryReference.setOnClickListener { referenceCleared() }
        lytTagsPreview.setOnClickListener { editTagsOnEntry() }

        fabEditEntryReference.setOnClickListener { executeActionAndCloseFloatingActionMenu { addReferenceToEntry() } }
        fabEditEntryAbstract.setOnClickListener { executeActionAndCloseFloatingActionMenu { addAbstractToEntry() } }

        setupEntryContentView()
    }

    private fun addReferenceToEntry() {
        forceShowReferencePreview = true
        setReferencePreviewOnUIThread()

        editReference()
    }

    private fun addAbstractToEntry() {
        forceShowAbstractPreview = true
        setAbstractPreviewOnUIThread()

        editAbstract()
    }

    private fun executeActionAndCloseFloatingActionMenu(action: (() -> Unit)) {
        action.invoke()
        fabEntryFieldsMenu.close(true)
    }

    private fun referenceCleared() {
        referenceToEdit = null

        entryHasBeenEdited()

        updateCanEntryBeSavedOnUIThread(true)
        setReferencePreviewOnUIThread()
    }

    private fun setupEntryContentView() {
        lytOnboardingText.setOnClickListener { editContent() }
        txtEntryContentLabel.setOnClickListener { editContent() }

        wbEntry.changeFullscreenModeListener = { mode -> handleChangeFullscreenModeEvent(mode) }

        wbEntry.singleTapListener = { handleWebViewSingleTap(it) }
        wbEntry.doubleTapListener = { handleWebViewDoubleTap(it) }
        wbEntry.swipeListener = { isInFullscreen, swipeDirection -> handleWebViewSwipe(isInFullscreen, swipeDirection) }

        val settings = wbEntry.getSettings()
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.defaultFontSize = 18 // default font is too small
        settings.domStorageEnabled = true // otherwise images may not load, see https://stackoverflow.com/questions/29888395/images-not-loading-in-android-webview
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptEnabled = true // so that embedded videos etc. work

        wbEntry.addJavascriptInterface(GetHtmlCodeFromWebViewJavaScripInterface { url, html -> siteFinishedLoading(url, html) }, GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        wbEntry.setWebChromeClient(object : WebChromeClient() {
            private var hasCompletelyFinishedLoadingPage = false
            private val timerCheckIfHasCompletelyFinishedLoadingPage = Timer()

            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                super.onProgressChanged(webView, newProgress)

                if(newProgress < 100) {
                    hasCompletelyFinishedLoadingPage = false
                }
                else {
                    hasCompletelyFinishedLoadingPage = true
                    timerCheckIfHasCompletelyFinishedLoadingPage.schedule(1000L) { // 100 % may only means a part of the page but not the whole page is loaded -> wait some time and check if not loading another part of the page
                        if(hasCompletelyFinishedLoadingPage) {
                            webPageCompletelyLoaded(webView)
                        }
                    }
                }
            }
        })
    }

    private fun webPageCompletelyLoaded(webView: WebView) {
        runOnUiThread { webPageCompletelyLoadedOnUiThread(webView) }
    }

    private fun webPageCompletelyLoadedOnUiThread(webView: WebView) {
        // if EntryExtractionResult's entry content hasn't been extracted yet, wait till WebView is loaded and extract entry content then
        if(entryExtractionResult != null && isInReaderMode == false && webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
            webView.loadUrl("javascript:$GetHtmlCodeFromWebViewJavaScriptInterfaceName.finishedLoadingSite" +
                    "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }
        else if(entry != null || readLaterArticle != null) {
            wbEntry.setWebViewClient(null) // now reactivate default url handling
        }
    }

    private fun siteFinishedLoading(url: String, html: String) {
        runOnUiThread { wbEntry.setWebViewClient(null) } // now reactivate default url handling

        // now try to extract entry content from WebView's html
        if(entryExtractionResult != null && isInReaderMode == false) {
            entryExtractionResult?.webSiteHtml = html
            contentToEdit = html

            if(entryExtractionResult?.couldExtractContent == false) {
                entryExtractionResult?.let { extractionResult ->
                    articleExtractorManager.extractArticleAndAddDefaultData(extractionResult, html, url)

                    if(extractionResult.couldExtractContent) {
                        runOnUiThread { extractedContentOnUiThread(extractionResult) }
                    }
                }
            }
        }
    }

    private fun extractedContentOnUiThread(extractionResult: EntryExtractionResult) {
        wbEntry.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        mnToggleReaderMode?.isVisible = extractionResult.couldExtractContent

        editEntryExtractionResult(extractionResult, false) // updates reference and abstract, but avoids that extracted content gets shown (this is important according to our
        // lawyer, user must click on toggleReaderMode menu first)
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

                mayShowSaveEntryChangesHelpOnUIThread()
            }
            else if(result.didDeleteReference) {
                referenceCleared()
            }
        }

        setContentPreviewOnUIThread()
    }

    private fun mayShowSaveEntryChangesHelpOnUIThread() {
        val localSettings = entryService.dataManager.localSettings

        if(localSettings.didShowSaveEntryChangesHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpSave, R.string.context_help_save_entry_changes)

            localSettings.didShowSaveEntryChangesHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
    }

    private fun savedReference(reference: Reference) {
        referenceToEdit = reference // do not set reference directly on entry as if entry is not saved yet adding it to reference.entries causes an error

        entryExtractionResult?.series = null
        readLaterArticle?.entryExtractionResult?.series = null

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
                    mayShowSaveEntryChangesHelpOnUIThread()
                }
            }
        }
    }

    private fun editReference() {
        setWaitingForResult(EditReferenceActivity.ResultId)

        val reference = referenceToEdit
        val entry = entry ?: readLaterArticle?.entryExtractionResult?.entry ?: entryExtractionResult?.entry ?: Entry("") // should never be the case that entry is null, just to make compiler happy

        presenter.editReference(reference, entry, getCurrentSeries())
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
                mayShowSaveEntryChangesHelpOnUIThread()
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
        val content = contentToEdit
        val url = reference?.url
        var showOnboarding = true

        if(shouldShowContent(content)) {
            showContentInWebView(content, url)
            showOnboarding = false
        }
        else if(isInReaderMode == false && entryExtractionResult?.webSiteHtml != null) {
            showContentInWebView(entryExtractionResult?.webSiteHtml, url)
            showOnboarding = false
        }
        else if(url != null) { // then load url
            clearWebViewEntry()
            wbEntry.setWebViewClient(WebViewClient()) // to avoid that redirects open url in browser
            wbEntry.loadUrl(url)
            showOnboarding = false
        }

        setOnboardingTextVisibilityOnUIThread(showOnboarding)
    }

    private fun shouldShowContent(content: String?): Boolean {
        // TODO: currently we assume that for entry and readLaterArticle content is always set, this may change in the feature
        return content.isNullOrBlank() == false &&
                (entryExtractionResult == null ||
                (isInReaderMode && entryExtractionResult?.couldExtractContent == true) )
    }

    private fun showContentInWebView(contentParam: String?, url: String?) {
        var content = contentParam

        if(content?.startsWith("<html") == false && content?.startsWith("<body") == false && content?.startsWith("<!doctype") == false) {
            content = "<html><body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\">" + content + "</body></html>"
        }

        clearWebViewEntry() // clear WebView
        if(url != null && Build.VERSION.SDK_INT > 16) {
            wbEntry.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", "utf-8", null)
        }
        else {
            wbEntry.loadData(content, "text/html; charset=UTF-8", null)
        }
    }

    private fun clearWebViewEntry() {
        if(Build.VERSION.SDK_INT < 18) {
            wbEntry.clearView()
        }
        else {
            wbEntry.loadUrl("about:blank")
        }
    }

    private fun setOnboardingTextVisibilityOnUIThread(showOnboarding: Boolean) {
        if(showOnboarding) {
            wbEntry.visibility = View.GONE
            lytOnboardingText.visibility = View.VISIBLE

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(R.string.activity_edit_entry_edit_content_onboarding_text).toString(), Html.FROM_HTML_MODE_LEGACY)
            }
            else {
                txtOnboardingText.text = Html.fromHtml(txtOnboardingText.context.getText(R.string.activity_edit_entry_edit_content_onboarding_text).toString())
            }
        }
        else {
            wbEntry.visibility = View.VISIBLE
            lytOnboardingText.visibility = View.GONE
        }
    }


    private fun setAbstractPreviewOnUIThread() {
        if(abstractToEdit.isNullOrBlank()) {
            lytAbstractPreview.setOnboardingTextOnUiThread(R.string.activity_edit_entry_abstract_onboarding_text)
        }
        else {
            lytAbstractPreview.setFieldValueOnUiThread(abstractToEdit.getPlainTextForHtml())
        }

        val showAbstractPreview = this.forceShowAbstractPreview || abstractToEdit.isNullOrBlank() == false

        lytAbstractPreview.visibility = if(showAbstractPreview) View.VISIBLE else View.GONE
        fabEditEntryAbstract.visibility = if(showAbstractPreview) View.GONE else View.VISIBLE
        setFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setReferencePreviewOnUIThread() {
        if(referenceToEdit == null) {
            lytReferencePreview.setOnboardingTextOnUiThread(R.string.activity_edit_entry_reference_onboarding_text)
        }
        else {
            lytReferencePreview.setFieldValueOnUiThread(referenceToEdit.getPreviewWithSeriesAndPublishingDate(getCurrentSeries()))
        }

        val showReferencePreview = this.forceShowReferencePreview || referenceToEdit != null

        lytReferencePreview.visibility = if(showReferencePreview) View.VISIBLE else View.GONE
        fabEditEntryReference.visibility = if(showReferencePreview) View.GONE else View.VISIBLE
        setFloatingActionButtonVisibilityOnUIThread()

        btnClearEntryReference.visibility = if(referenceToEdit == null) View.GONE else View.VISIBLE
        mnShareEntry?.isVisible = referenceToEdit?.url.isNullOrBlank() == false
    }

    private fun getCurrentSeries(): Series? {
        readLaterArticle?.let { return it.entryExtractionResult.series }

        entryExtractionResult?.let { return it.series }

        return referenceToEdit?.series
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        if(tagsOnEntry.filterNotNull().isEmpty()) {
            lytTagsPreview.setOnboardingTextOnUiThread(R.string.activity_edit_entry_tags_onboarding_text)
        }
        else {
            val tagsPreview = tagsOnEntry.filterNotNull().sortedBy { it.name.toLowerCase() }.joinToString { it.name }
            lytTagsPreview.setFieldValueOnUiThread(tagsPreview)
        }
    }

    private fun setFloatingActionButtonVisibilityOnUIThread() {
        if(fabEditEntryReference.visibility != View.GONE || fabEditEntryAbstract.visibility != View.GONE) {
            fabEntryFieldsMenu.visibility = View.VISIBLE
        }
        else {
            fabEntryFieldsMenu.visibility = View.GONE
        }
    }


    private fun handleChangeFullscreenModeEvent(mode: FullscreenWebView.FullscreenMode) {
        when(mode) {
            FullscreenWebView.FullscreenMode.Enter -> enterFullscreenMode()
            FullscreenWebView.FullscreenMode.Leave -> leaveFullscreenMode()
        }
    }

    private fun handleWebViewSingleTap(isInFullscreen: Boolean) {
        if(isInFullscreen == false) {
            editContent()
        }
    }

    private fun handleWebViewDoubleTap(isInFullscreen: Boolean) {
        if(isInFullscreen) {
            mayShowEntryInformationFullscreenGesturesHelpOnUIThread { saveEntryAndCloseDialog() }
        }
    }

    private fun handleWebViewSwipe(isInFullscreen: Boolean, swipeDirection: OnSwipeTouchListener.SwipeDirection) {
        if(isInFullscreen) {
            when(swipeDirection) {
                OnSwipeTouchListener.SwipeDirection.Left -> {
                    mayShowEntryInformationFullscreenGesturesHelpOnUIThread { presenter.returnToPreviousView() }
                }
                OnSwipeTouchListener.SwipeDirection.Right -> {
                    mayShowEntryInformationFullscreenGesturesHelpOnUIThread { editTagsOnEntry() }
                }
            }
        }
    }

    private fun mayShowEntryInformationFullscreenGesturesHelpOnUIThread(userConfirmedHelpOnUIThread: () -> Unit) {
        val localSettings = entryService.dataManager.localSettings

        if(localSettings.didShowEntryInformationFullscreenGesturesHelp == false) {
            dialogService.showConfirmationDialog(getString(R.string.context_help_entry_information_fullscreen_gestures), showNoButton = false) {
                runOnUiThread {
                    wbEntry.leaveFullscreenMode() // leave fullscreen otherwise a lot of unwanted behaviour occurs
                    userConfirmedHelpOnUIThread()
                }
            }

            localSettings.didShowEntryInformationFullscreenGesturesHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
        else {
            wbEntry.leaveFullscreenMode() // leave fullscreen otherwise a lot of unwanted behaviour occurs
            userConfirmedHelpOnUIThread()
        }
    }


    private fun leaveFullscreenMode() {
        lytEntryFieldsPreview.visibility = View.VISIBLE
        txtEntryContentLabel.visibility = View.VISIBLE
        appBarLayout.visibility = View.VISIBLE
        setFloatingActionButtonVisibilityOnUIThread()

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = false
        wbEntry.layoutParams = layoutParams
    }

    private fun enterFullscreenMode() {
        lytEntryFieldsPreview.visibility = View.GONE
        txtEntryContentLabel.visibility = View.GONE
        fabEntryFieldsMenu.visibility = View.GONE
        appBarLayout.visibility = View.GONE

        val layoutParams = wbEntry.layoutParams as RelativeLayout.LayoutParams
        layoutParams.alignWithParent = true
        wbEntry.layoutParams = layoutParams

        content_layout_root.invalidate()

        mayShowEntryInformationFullscreenHelpOnUIThread()
    }

    private fun mayShowEntryInformationFullscreenHelpOnUIThread() {
        val localSettings = entryService.dataManager.localSettings

        if(localSettings.didShowEntryInformationFullscreenHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpFullscreenMode, R.string.context_help_entry_information_fullscreen)

            localSettings.didShowEntryInformationFullscreenHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
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
        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        mnSaveEntry = menu.findItem(R.id.mnSaveEntry)

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        mnToggleReaderMode?.isVisible = entryExtractionResult?.couldExtractContent == true /*&& entryExtractionResult?.webSiteHtml != null*/ // show mnToggleReaderMode only if previously original web site was shown

        mnSaveEntryExtractionResultForLaterReading = menu.findItem(R.id.mnSaveEntryExtractionResultForLaterReading)
        mnSaveEntryExtractionResultForLaterReading?.isVisible = entryExtractionResult != null

        mnDeleteReadLaterArticle = menu.findItem(R.id.mnDeleteReadLaterArticle)
        mnDeleteReadLaterArticle?.isVisible = readLaterArticle != null

        mnShareEntry = menu.findItem(R.id.mnShareEntry)
        mnShareEntry?.isVisible = referenceToEdit?.url.isNullOrBlank() == false

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
            presenter.shareEntry(entry, entry.reference, entry.reference?.series)
        }

        readLaterArticle?.entryExtractionResult?.let {  extractionResult ->
            presenter.shareEntry(extractionResult.entry, extractionResult.reference, extractionResult.series)
        }

        entryExtractionResult?.let { extractionResult ->
            presenter.shareEntry(extractionResult.entry, extractionResult.reference, extractionResult.series)
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
            presenter.saveEntryAsync(entry, referenceToEdit, tags = tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntry = true, savedEntry = entry))
                }
                callback(successful)
            }
        }

        entryExtractionResult?.let { extractionResult ->
            // TODO: save extracted content when in reader mode and webSiteHtml when not in reader mode
            // TODO: contentToEdit show now always contain the correct value depending on is or is not in reader mode, doesn't it?

            updateEntry(extractionResult.entry, content, abstract)
            presenter.saveEntryAsync(extractionResult.entry, referenceToEdit, extractionResult.series, tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedEntry = extractionResult.entry))
                }
                callback(successful)
            }
        }

        readLaterArticle?.let { readLaterArticle ->
            val extractionResult = readLaterArticle.entryExtractionResult
            updateEntry(extractionResult.entry, content, abstract)

            presenter.saveEntryAsync(extractionResult.entry, referenceToEdit, extractionResult.series, tagsOnEntry) { successful ->
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
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_entry_alert_message_entry_contains_unsaved_changes)) { shouldChangesGetSaved ->
            runOnUiThread {
                if(shouldChangesGetSaved) {
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
        if(parameters != null) {
            parameters.entry?.let { editEntry(it) }

            parameters.readLaterArticle?.let { editReadLaterArticle(it) }

            parameters.entryExtractionResult?.let {
                isInReaderMode = it.couldExtractContent
                editEntryExtractionResult(it)
            }

            if(parameters.createEntry) {
                createEntry()
            }
        }
    }

    private fun createEntry(editContent: Boolean = true) {
        canEntryBeSaved = true

        editEntry(Entry(""))

        if(editContent) {
            editContent() // go directly to edit content dialog, there's absolutely nothing to see on this almost empty screen
        }
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

    private fun editEntryExtractionResult(extractionResult: EntryExtractionResult, updateContentPreview: Boolean = true) {
        this.entryExtractionResult = extractionResult
        canEntryBeSaved = true

        editEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags, updateContentPreview)
    }

    private fun editEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?, updateContentPreview: Boolean = true) {
        contentToEdit = entry?.content
        abstractToEdit = entry?.abstractString
        referenceToEdit = reference

        if(updateContentPreview) {
            setContentPreviewOnUIThread(reference)
        }

        if(abstractToEdit.isNullOrBlank() == false) { this.forceShowAbstractPreview = true } // forcing that once it has been shown it doesn't get hidden anymore
        setAbstractPreviewOnUIThread()

        reference?.let { this.forceShowReferencePreview = true } // forcing that once it has been shown it doesn't get hidden anymore
        setReferencePreviewOnUIThread()

        tags?.forEach { tag ->
            if(tagsOnEntry.contains(tag) == false) { // to avoid have a tag twice we really have to check each single tag
                tagsOnEntry.add(tag)
            }
        }

        setTagsOnEntryPreviewOnUIThread()

        mayRegisterEventBusListener()
    }

    private fun restoreReference(referenceId: String) {
        referenceToEdit = referenceService.retrieve(referenceId)

        runOnUiThread { setReferencePreviewOnUIThread() }
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
        if(entry?.isPersisted() ?: false && eventBusListener == null) {
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
