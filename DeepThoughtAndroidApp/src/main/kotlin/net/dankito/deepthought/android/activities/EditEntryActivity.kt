package net.dankito.deepthought.android.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.webkit.*
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.activity_edit_entry.*
import kotlinx.android.synthetic.main.view_floating_action_button_entry_fields.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.*
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.fields.ItemField
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.richtexteditor.android.animation.ShowHideViewAnimator
import net.dankito.service.data.*
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import net.engio.mbassy.listener.Handler
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class EditEntryActivity : BaseActivity() {

    companion object {
        private const val STATE_NAME_PREFIX_INTENT_EXTRA_NAME = "STATE_NAME_PREFIX"

        private const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        private const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        private const val FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_TAGS_PREVIEW"
        private const val FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_REFERENCE_PREVIEW"
        private const val FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_ABSTRACT_PREVIEW"

        private const val IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME = "IS_IN_EDIT_CONTENT_MODE"
        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val EDIT_CONTENT_HTML_INTENT_EXTRA_NAME = "EDIT_CONTENT_HTML"
        private const val ABSTRACT_INTENT_EXTRA_NAME = "ABSTRACT"
        private const val REFERENCE_INTENT_EXTRA_NAME = "REFERENCE"
        private const val TAGS_ON_ENTRY_INTENT_EXTRA_NAME = "TAGS_ON_ENTRY"

        const val ResultId = "EDIT_ENTRY_ACTIVITY_RESULT"

        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"

        private const val ShowHideEditContentViewAnimationDurationMillis = 500L

        private const val ShowHideEditTagsAnimationDurationMillis = 250L

        private val log = LoggerFactory.getLogger(EditEntryActivity::class.java)
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
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var threadPool: IThreadPool

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var extractArticleHandler: ExtractArticleHandler

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var eventBus: IEventBus


    private var item: Item? = null

    private var readLaterArticle: ReadLaterArticle? = null

    private var itemExtractionResult: ItemExtractionResult? = null


    private var originalContent: String? = null

    private var originalTags: MutableCollection<Tag>? = null

    private var originalSource: Source? = null

    private var originalTitleAbstract: String? = null


    private var contentToEdit: String? = null

    private var abstractToEdit: String? = null

    private var sourceToEdit: Source? = null

    private val tagsOnEntry: MutableList<Tag> = ArrayList()

    private val changedFields = HashSet<ItemField>()

    private var forceShowTagsPreview = false

    private var forceShowReferencePreview = false

    private var forceShowAbstractPreview = false


    private val presenter: EditEntryPresenter

    private var isInEditContentMode = false

    private var isInReaderMode = false

    private var webSiteHtml: String? = null

    private var isLoadingUrl = false


    private val contextHelpUtil = ContextHelpUtil()

    private val toolbarUtil = ToolbarUtil()

    private val openUrlOptionsView = OpenUrlOptionsView()

    private lateinit var editHtmlView: EditHtmlView

    private val animator = ShowHideViewAnimator()

    private var mnSaveEntry: MenuItem? = null

    private var mnDeleteExistingEntry: MenuItem? = null

    private var mnToggleReaderMode: MenuItem? = null

    private var mnSaveEntryExtractionResultForLaterReading: MenuItem? = null

    private var mnDeleteReadLaterArticle: MenuItem? = null

    private var mnShareEntry: MenuItem? = null

    private lateinit var floatingActionMenu: EditEntryActivityFloatingActionMenuButton


    private val dataManager: DataManager

    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        dataManager = entryService.dataManager

        presenter = EditEntryPresenter(entryPersister, readLaterArticleService, clipboardService, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditEntryActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        if(savedInstanceState == null) {
            showParameters(getParameters() as? EditEntryActivityParameters)
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val stateNamePrefix = savedInstanceState.getString(STATE_NAME_PREFIX_INTENT_EXTRA_NAME)

        this.forceShowTagsPreview = savedInstanceState.getBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowReferencePreview = savedInstanceState.getBoolean(FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowAbstractPreview = savedInstanceState.getBoolean(FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME, false)

        this.isInEditContentMode = savedInstanceState.getBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, false)
        this.isInReaderMode = savedInstanceState.getBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, false)

        (getAndClearState(getKeyForState(stateNamePrefix, ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)) as? ItemExtractionResult)?.let { editEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }

        if(itemExtractionResult == null && savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME) == null &&
                savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME) == null) { // a new Item is being created then
            createEntry(false) // don't go to EditHtmlTextDialog for content here as we're restoring state, content may already be set
        }

        getAndClearStringState(getKeyForState(stateNamePrefix, CONTENT_INTENT_EXTRA_NAME))?.let { content ->
            contentToEdit = content
            setContentPreviewOnUIThread()
        }

        savedInstanceState.getString(ABSTRACT_INTENT_EXTRA_NAME)?.let { abstract ->
            abstractToEdit = abstract
            setAbstractPreviewOnUIThread()
        }

        if(savedInstanceState.containsKey(REFERENCE_INTENT_EXTRA_NAME)) {
            restoreReference(savedInstanceState.getString(REFERENCE_INTENT_EXTRA_NAME))
        }

        savedInstanceState.getString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME)?.let { tagsOnEntryIds -> restoreTagsOnEntryAsync(tagsOnEntryIds) }

        wbvwContent.restoreInstanceState(savedInstanceState)

        floatingActionMenu.restoreInstanceState(savedInstanceState)

        getAndClearStringState(getKeyForState(stateNamePrefix, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME))?.let { editHtmlView.setHtml(it, sourceToEdit?.url) }

        if(isInEditContentMode) {
            editContent()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            val stateNamePrefix = getStateNamePrefix()
            outState.putString(STATE_NAME_PREFIX_INTENT_EXTRA_NAME, stateNamePrefix) // we need to save the state name prefix as on restore we wouldn't otherwise know how to  restore ItemExtractionResult as state name prefix is derived from it

            outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, null)
            item?.id?.let { entryId -> outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, entryId) }

            outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, null)
            readLaterArticle?.id?.let { readLaterArticleId -> outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticleId) }

            itemExtractionResult?.let { storeState(getKeyForState(stateNamePrefix, ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME), it) }

            outState.putBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, forceShowTagsPreview)
            outState.putBoolean(FORCE_SHOW_REFERENCE_PREVIEW_INTENT_EXTRA_NAME, forceShowReferencePreview)
            outState.putBoolean(FORCE_SHOW_ABSTRACT_PREVIEW_INTENT_EXTRA_NAME, forceShowAbstractPreview)

            outState.putBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, isInEditContentMode)
            outState.putBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, isInReaderMode)

            outState.putString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME, serializer.serializeObject(tagsOnEntry))

            if(sourceToEdit == null || sourceToEdit?.id != null) { // save value only if source has been deleted or a persisted source is set (-> don't store ItemExtractionResult's or ReadLaterArticle's unpersisted source)
                outState.putString(REFERENCE_INTENT_EXTRA_NAME, sourceToEdit?.id)
            }

            outState.putString(ABSTRACT_INTENT_EXTRA_NAME, abstractToEdit)

            if(contentToEdit != originalContent) {
                storeState(getKeyForState(stateNamePrefix, CONTENT_INTENT_EXTRA_NAME), contentToEdit) // application crashes if objects put into bundle are too large (> 1 MB) for Android
            }

            if(isInEditContentMode) {
                storeState(getKeyForState(stateNamePrefix, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME), editHtmlView.getHtml()) // application crashes if objects put into bundle are too large (> 1 MB) for Android
            }

            wbvwContent.onSaveInstanceState(outState)

            floatingActionMenu.saveInstanceState(outState)
        }
    }

    private fun getKeyForState(stateNamePrefix: String, stateName: String): String {
        return stateNamePrefix + "_" + stateName
    }

    /**
     * As there may be multiple EditEntryActivities opened at a time (e.g. when the user selected multiple articles to view), get a unique key prefix for each opened activity as
     * otherwise they overwrite their states.
     */
    private fun getStateNamePrefix(): String {
        item?.id?.let { return it }

        readLaterArticle?.id?.let { return it }

        itemExtractionResult?.source?.url?.let { return it }

        return "" // an unpersisted Item -> currently there's no way to have two EditEntryActivities for two unpersisted items in parallel
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_entry)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }

        lytAbstractPreview.setFieldNameOnUiThread(R.string.activity_edit_item_title_summary_label) { didAbstractChange -> abstractChanged(didAbstractChange) }
        lytAbstractPreview.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                appliedChangesToAbstract(lytAbstractPreview.didValueChange)
            }
        }

        lytReferencePreview.didValueChangeListener = { didSourceTitleChange -> sourceTitleChanged(didSourceTitleChange) }

        lytTagsPreview.didValueChangeListener = { didTagsChange ->
            entryPropertySet()
            updateEntryFieldChangedOnUIThread(ItemField.Tags, didTagsChange)
        }
        lytTagsPreview.fieldValueFocusChangedListener = { hasFocus -> tagsPreviewFocusChanged(hasFocus) }

        wbvwContent?.requestFocus() // avoid that lytAbstractPreview gets focus and keyboard therefore gets show on activity start

        floatingActionMenu = EditEntryActivityFloatingActionMenuButton(findViewById(R.id.floatingActionMenu) as FloatingActionMenu, { addTagsToEntry() },
                { addReferenceToEntry() }, { addAbstractToEntry() } )

        setupEntryContentView()

        setupEntryContentEditor()
    }

    private fun setupEntryContentEditor() {
        editHtmlView = EditHtmlView(this)
        editHtmlView.setupHtmlEditor(lytEditContent)

        editHtmlView.setHtmlChangedCallback { didChange ->
            runOnUiThread { updateEntryFieldChangedOnUIThread(ItemField.Content,didChange)  }
        }
    }

    private fun addTagsToEntry() {
        editTagsOnEntry()

        forceShowTagsPreview = true
        setTagsOnEntryPreviewOnUIThread()
    }

    private fun addReferenceToEntry() {
        editReference()

        forceShowReferencePreview = true
        setReferencePreviewOnUIThread()
    }

    private fun addAbstractToEntry() {
        forceShowAbstractPreview = true
        setAbstractPreviewOnUIThread()

        lytAbstractPreview.startEditing()
    }

    private fun setupEntryContentView() {
        lytViewContent.setOnClickListener { lytViewContent.requestFocus() } // so that EditEntityField previews loose focus

        wbvwContent.setOptionsBar(lytFullscreenWebViewOptionsBar)
        wbvwContent.changeFullscreenModeListener = { mode -> handleChangeFullscreenModeEvent(mode) }

        wbvwContent.singleTapListener = { handleWebViewSingleTap(it) }
        wbvwContent.swipeListener = { isInFullscreen, swipeDirection -> handleWebViewSwipe(isInFullscreen, swipeDirection) }

        fixThatWebViewIsLoadingVerySlow()

        val settings = wbvwContent.settings
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.defaultFontSize = 18 // default font is too small
        settings.domStorageEnabled = true // otherwise images may not load, see https://stackoverflow.com/questions/29888395/images-not-loading-in-android-webview
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptEnabled = true // so that embedded videos etc. work

        wbvwContent.addJavascriptInterface(GetHtmlCodeFromWebViewJavaScripInterface { url, html -> siteFinishedLoading(url, html) }, GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        wbvwContent.setWebChromeClient(object : WebChromeClient() {
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

    private val webViewClient = object : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            request?.url?.toString()?.let { url ->
                userClickedOnUrl(url)
            }

            return true
        }

        @SuppressWarnings("deprecation")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            userClickedOnUrl(url)

            return true
        }
    }

    private fun fixThatWebViewIsLoadingVerySlow() {
        // avoid that WebView is loading very, very slow, see https://stackoverflow.com/questions/7422427/android-webview-slow
        // but actually had no effect on my side

        wbvwContent.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wbvwContent.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        else {
            wbvwContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            wbvwContent.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        }
    }

    private fun webPageCompletelyLoaded(webView: WebView) {
        runOnUiThread { webPageCompletelyLoadedOnUiThread(webView) }
    }

    private fun webPageCompletelyLoadedOnUiThread(webView: WebView) {
        // if ItemExtractionResult's item content hasn't been extracted yet, wait till WebView is loaded and extract item content then
        if((itemExtractionResult != null || readLaterArticle != null) && isInReaderMode == false &&
                webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
            webView.loadUrl("javascript:$GetHtmlCodeFromWebViewJavaScriptInterfaceName.finishedLoadingSite" +
                    "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }
        else if(item != null) {
            urlLoadedNow()
        }
    }

    private fun siteFinishedLoading(url: String, html: String) {
        urlLoadedNow()

        // now try to extract item content from WebView's html
        val extractionResult = itemExtractionResult ?: readLaterArticle?.itemExtractionResult
        if(extractionResult != null && isInReaderMode == false) {
            webSiteHtml = html
            contentToEdit = html

            if(extractionResult?.couldExtractContent == false) {
                extractionResult?.let { extractionResult ->
                    articleExtractorManager.extractArticleUserDidSeeBefore(extractionResult, html, url)

                    if(extractionResult.couldExtractContent) {
                        runOnUiThread { extractedContentOnUiThread(extractionResult) }
                    }
                }
            }
        }
    }

    private fun urlLoadedNow() {
        isLoadingUrl = false
        wbvwContent.elementClickedListener = null

        runOnUiThread {
            wbvwContent.setWebViewClient(webViewClient) // now reactivate default url handling
            prgIsLoadingWebPage.visibility = View.GONE
        }
    }

    private fun extractedContentOnUiThread(extractionResult: ItemExtractionResult) { // extractionResult can either be from itemExtractionResult or readLaterArticle
        wbvwContent.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        mnToggleReaderMode?.isVisible = extractionResult.couldExtractContent
        invalidateOptionsMenu()

        itemExtractionResult?.let {
            editEntryExtractionResult(it, false) // updates source and abstract, but avoids that extracted content gets shown (this is important according to our
            // lawyer, user must click on toggleReaderMode menu first)
        }

        readLaterArticle?.let { editReadLaterArticle(it, false) }

        mayShowReaderViewHelp()
    }

    private fun mayShowReaderViewHelp() {
        val localSettings = entryService.dataManager.localSettings
        if(localSettings.didShowReaderViewHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpReaderView, R.string.context_help_reader_view) {
                setFloatingActionButtonVisibilityOnUIThread()
                localSettings.didShowReaderViewHelp = true
                entryService.dataManager.localSettingsUpdated()
            }

            setFloatingActionButtonVisibilityOnUIThread()
        }
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
            lytReferencePreview.editingSourceDone(result)
        }

        setContentPreviewOnUIThread()

        mayRegisterEventBusListener()
    }

    private fun sourceTitleChanged(didSourceTitleChange: Boolean) {
        updateEntryFieldChangedOnUIThread(ItemField.SourceTitle, didSourceTitleChange)
    }


    private fun editContent() {
        if(isLoadingUrl) { // while WebView is still loading contentToEdit is not set yet (contentToEdit gets set when loading finishes)
            return
        }

        contentToEdit?.let { content ->
            editHtmlView.setHtml(content, sourceToEdit?.url)

            txtEnterContentHint.visibility =
                    if(content.isBlank() == false || dataManager.localSettings.didShowAddItemPropertiesHelp) View.GONE
                    else View.VISIBLE

            val webViewContentLocation = IntArray(2)
            wbvwContent.getLocationInWindow(webViewContentLocation)
            val start = webViewContentLocation[1].toFloat()
            lytViewContent.visibility = View.GONE
            lytEditContent.visibility = View.VISIBLE
            playShowEditContentViewAnimation(start)
            setFloatingActionButtonVisibilityOnUIThread()

            isInEditContentMode = true

            invalidateOptionsMenu()
            contentEditor.focusEditorAndShowKeyboardDelayed()
        }
    }

    private fun playShowEditContentViewAnimation(start: Float) {
        val interpolator = AccelerateInterpolator()

        val fieldsPreviewYAnimator = ObjectAnimator
                .ofFloat(lytEntryFieldsPreview, View.Y, lytEntryFieldsPreview.top.toFloat(), -1 * lytEntryFieldsPreview.measuredHeight.toFloat())
                .setDuration(ShowHideEditContentViewAnimationDurationMillis)
        fieldsPreviewYAnimator.interpolator = interpolator

        val editContentViewYAnimator = ObjectAnimator
                .ofFloat(lytEditContent, View.Y, start, 0f)
                .setDuration(ShowHideEditContentViewAnimationDurationMillis)
        editContentViewYAnimator.interpolator = interpolator

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fieldsPreviewYAnimator, editContentViewYAnimator)
        animatorSet.start()
    }

    private fun isEditingContent() = lytEditContent.visibility == View.VISIBLE

    private fun appliedChangesToContent() {
        contentToEdit = editHtmlView.getHtml()

        runOnUiThread {
            leaveEditContentView()

            updateEntryFieldChangedOnUIThread(ItemField.Content, originalContent != contentToEdit)
            setContentPreviewOnUIThread()
        }
    }

    private fun leaveEditContentView() {
        contentEditor.hideKeyboard()

        animator.playShowAnimation(lytEntryFieldsPreview)
        lytEditContent.visibility = View.GONE
        lytViewContent.visibility = View.VISIBLE
        setFloatingActionButtonVisibilityOnUIThread()

        isInEditContentMode = false

        invalidateOptionsMenu()
    }

    private fun abstractChanged(didAbstractChange: Boolean) {
        abstractToEdit = lytAbstractPreview.getCurrentFieldValue()
        entryPropertySet()

        runOnUiThread {
            updateEntryFieldChangedOnUIThread(ItemField.TitleOrSummary, didAbstractChange)
        }
    }

    private fun appliedChangesToAbstract(didAbstractChange: Boolean) {
        abstractToEdit = lytAbstractPreview.getCurrentFieldValue()
        entryPropertySet()

        runOnUiThread {
            updateEntryFieldChangedOnUIThread(ItemField.TitleOrSummary, didAbstractChange)
            setAbstractPreviewOnUIThread()
        }
    }

    private fun editReference() {
        lytReferencePreview.visibility = View.VISIBLE
        lytReferencePreview.startEditing()
    }

    private fun setSourceToEdit(source: Source?) {
        sourceToEdit = source

        updateEntryFieldChangedOnUIThread(ItemField.Source, source != originalSource)

        entryPropertySet() // TODO: still senseful?

        updateShowMenuItemShareEntry()
    }

    private fun editTagsOnEntry() {
        lytTagsPreview.visibility = View.VISIBLE
        lytTagsPreview.startEditing()
    }

    private fun updateEntryFieldChangedOnUIThread(field: ItemField, didChange: Boolean) {
        if(didChange) {
            changedFields.add(field)
        }
        else {
            changedFields.remove(field)
        }

        setMenuSaveEntryVisibleStateOnUIThread()
    }

    private fun setMenuSaveEntryVisibleStateOnUIThread() {
        if(haveAllFieldsOfExistingEntryBeenCleared()) {
            mnSaveEntry?.isVisible = false
        }
        else {
            mnSaveEntry?.isVisible = item == null // ItemExtractionResult and ReadLaterArticle always can be saved
                    || changedFields.size > 0
        }
    }

    private fun haveAllFieldsOfExistingEntryBeenCleared(): Boolean {
        if(item != null && item?.isPersisted() == true) {
            return haveAllFieldsBeenCleared()
        }

        return false
    }

    private fun haveAllFieldsBeenCleared(): Boolean {
        return contentToEdit.isNullOrBlank() && tagsOnEntry.isEmpty() && sourceToEdit == null && abstractToEdit.isNullOrBlank()
    }

    private fun entryPropertySet() {
        val localSettings = entryService.dataManager.localSettings

        if(localSettings.didShowAddItemPropertiesHelp == false && contentToEdit.isNullOrBlank() == false) {
            localSettings.didShowAddItemPropertiesHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
    }


    private fun setContentPreviewOnUIThread() {
        setContentPreviewOnUIThread(sourceToEdit)
    }

    private fun setContentPreviewOnUIThread(source: Source?) {
        val content = contentToEdit
        val url = source?.url
        var showContentOnboarding = true
        prgIsLoadingWebPage.visibility = View.GONE

        if(shouldShowContent(content)) {
            showContentInWebView(content, url)
            wbvwContent.setWebViewClient(webViewClient)
            showContentOnboarding = false
        }
        else if(isInReaderMode == false && webSiteHtml != null) {
            showContentInWebView(webSiteHtml, url)
            wbvwContent.setWebViewClient(webViewClient)
            showContentOnboarding = false
        }
        else if(url != null && item == null) { // then load url (but don't show it for an Item)
            clearWebViewEntry()
            isLoadingUrl = true
            wbvwContent.elementClickedListener = { true } // disable link clicks during loading url
            wbvwContent.setWebViewClient(WebViewClient()) // to avoid that redirects open url in browser
            prgIsLoadingWebPage.visibility = View.VISIBLE
            wbvwContent.loadUrl(url)
            showContentOnboarding = false
        }

        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread(showContentOnboarding)
    }

    private fun shouldShowContent(content: String?): Boolean {
        // TODO: currently we assume that for item content is always set, this may change in the feature
        return content.isNullOrBlank() == false &&
                (item != null || (isInReaderMode &&
                        (itemExtractionResult?.couldExtractContent == true || readLaterArticle?.itemExtractionResult?.couldExtractContent == true)) )
    }

    private fun showContentInWebView(contentParam: String?, url: String?) {
        var content = contentParam

        if(content?.startsWith("<html") == false && content?.startsWith("<body") == false && content?.startsWith("<!doctype") == false) {
            content = "<html><body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\">" + content + "</body></html>"
        }

        clearWebViewEntry() // clear WebView
        if(url != null && Build.VERSION.SDK_INT > 16) {
            wbvwContent.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", "utf-8", null)
        }
        else {
            wbvwContent.loadData(content, "text/html; charset=UTF-8", null)
        }
    }

    private fun clearWebViewEntry() {
        if(Build.VERSION.SDK_INT < 18) {
            wbvwContent.clearView()
        }
        else {
            wbvwContent.loadUrl("about:blank")
        }
    }

    private fun setOnboardingTextVisibilityOnUIThread(showContentOnboarding: Boolean? = null) {
        val showOnboardingForEntryProperties = shouldShowOnboardingForEntryProperties()
        if(showContentOnboarding == true || showOnboardingForEntryProperties) {
            lytOnboardingText.visibility = View.VISIBLE
            lytContentWebViewAndOnboardingText.setOnClickListener { editContent() } // only enable editing content by clicking on lytContentWebViewAndOnboardingText when showing onboarding text

            val onboardingTextId = if(showContentOnboarding == true) R.string.activity_edit_item_edit_content_onboarding_text else R.string.activity_edit_item_add_item_properties_onboarding_text
            val onboardingText = if(showContentOnboarding == true) getText(onboardingTextId).toString() else getText(onboardingTextId).toString()
            txtOnboardingText.text = contextHelpUtil.stringUtil.getSpannedFromHtml(onboardingText)

            arrowToFloatingActionButton.visibility = if(showContentOnboarding != true && showOnboardingForEntryProperties) View.VISIBLE else View.GONE
        }
        else {
            lytOnboardingText.visibility = View.GONE
            lytContentWebViewAndOnboardingText.setOnClickListener(null)
        }

        if(showContentOnboarding == true) {
            lytContentWebView.visibility = View.GONE
        }
        else if(showContentOnboarding == false) {
            lytContentWebView.visibility = View.VISIBLE
        }
    }

    private fun shouldShowOnboardingForEntryProperties(): Boolean {
        return entryService.dataManager.localSettings.didShowAddItemPropertiesHelp == false &&
                lytTagsPreview.visibility == View.GONE && lytReferencePreview.visibility == View.GONE && lytAbstractPreview.visibility == View.GONE
    }


    private fun setAbstractPreviewOnUIThread() {
        lytAbstractPreview.setFieldNameOnUiThread(if(alsoShowTitleForSummary()) R.string.activity_edit_item_title_summary_label else R.string.activity_edit_item_summary_only_label)

        if(abstractToEdit.isNullOrBlank()) {
//            lytAbstractPreview.setOnboardingTextOnUiThread(R.string.activity_edit_entry_abstract_onboarding_text)
        }
        else {
            lytAbstractPreview.setFieldValueOnUiThread(abstractToEdit.getPlainTextForHtml())
        }

        val showAbstractPreview = this.forceShowAbstractPreview || abstractToEdit.isNullOrBlank() == false

        lytAbstractPreview.visibility = if(showAbstractPreview) View.VISIBLE else View.GONE
        if(fabEditEntryAbstract.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditEntryAbstract.visibility = if(showAbstractPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun alsoShowTitleForSummary(): Boolean {
        return sourceToEdit?.url == null && (abstractToEdit?.length ?: 0) < 35
    }

    private fun setReferencePreviewOnUIThread() {
        val showReferencePreview = this.forceShowReferencePreview || sourceToEdit != null

        lytReferencePreview.visibility = if(showReferencePreview) View.VISIBLE else View.GONE
        if(fabEditEntryReference.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditEntryReference.visibility = if(showReferencePreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()

        updateShowMenuItemShareEntry()
    }

    private fun updateShowMenuItemShareEntry() {
        mnShareEntry?.isVisible = sourceToEdit?.url.isNullOrBlank() == false
    }

    private fun getCurrentSeries(): Series? {
        readLaterArticle?.let { return it.itemExtractionResult.series }

        itemExtractionResult?.let { return it.series }

        return sourceToEdit?.series
    }

    private fun tagsPreviewFocusChanged(hasFocus: Boolean) {
        if(hasFocus) {
            lytTagsPreview.visibility = View.VISIBLE

            if(lytReferencePreview.visibility == View.VISIBLE || lytAbstractPreview.visibility == View.VISIBLE) {
                lytTagsPreview.executeActionAfterMeasuringHeight {
                    playHideOtherItemFieldsPreviewExceptTagsAnimation()
                }
            }
        }
        else {
            restoreLayoutEntryFieldsPreview()
        }
    }

    private fun restoreLayoutEntryFieldsPreview() {
        if(lytReferencePreview.measuredHeight > 0) { // only if it has been visible before
            lytReferencePreview.y = lytReferencePreview.top.toFloat()
            lytReferencePreview.visibility = View.VISIBLE
        }

        if(lytAbstractPreview.measuredHeight > 0) {
            lytAbstractPreview.y = lytAbstractPreview.top.toFloat()
            lytAbstractPreview.visibility = View.VISIBLE
        }
    }

    private fun playHideOtherItemFieldsPreviewExceptTagsAnimation() {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(createAnimatorsToHideOtherItemFieldsPreviewExceptTags())

        animatorSet.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator?) { }

            override fun onAnimationRepeat(animation: Animator?) { }

            override fun onAnimationCancel(animation: Animator?) { }

            override fun onAnimationEnd(animation: Animator?) {
                forceLayoutUpdateAfterHideOtherItemFieldsPreviewExceptTagsAnimation()
            }

        })

        animatorSet.start()
    }

    // don't know why we have to force layout to update
    private fun forceLayoutUpdateAfterHideOtherItemFieldsPreviewExceptTagsAnimation() {
        lytReferencePreview.visibility = View.GONE
        lytAbstractPreview.visibility = View.GONE
        lytTagsPreview.y = lytTagsPreview.top.toFloat()

        lytEntryFieldsPreview.invalidate()
        lytEntryFieldsPreview.forceLayout()
        lytEntryFieldsPreview.invalidate()
        lytEntryFieldsPreview.forceLayout()
    }

    private fun createAnimatorsToHideOtherItemFieldsPreviewExceptTags(): ArrayList<Animator> {
        val animators = ArrayList<Animator>()
        val interpolator = AccelerateInterpolator()

        if(lytReferencePreview.visibility == View.VISIBLE) {
            val sourcePreviewYAnimator = ObjectAnimator
                    .ofFloat(lytReferencePreview, View.Y, lytReferencePreview.top.toFloat(), -1 * lytReferencePreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            sourcePreviewYAnimator.interpolator = interpolator

            animators.add(sourcePreviewYAnimator)
        }

        if(lytAbstractPreview.visibility == View.VISIBLE) {
            val summaryPreviewYAnimator = ObjectAnimator
                    .ofFloat(lytAbstractPreview, View.Y, lytAbstractPreview.top.toFloat(), -1 * lytAbstractPreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            summaryPreviewYAnimator.interpolator = interpolator

            animators.add(summaryPreviewYAnimator)
        }

        val location = IntArray(2)
        lytEntryFieldsPreview.getLocationOnScreen(location)

        val tagsPreviewYAnimator = ObjectAnimator
                .ofFloat(lytTagsPreview, View.Y, lytTagsPreview.top.toFloat(), location[1].toFloat())
                .setDuration(ShowHideEditTagsAnimationDurationMillis)
        tagsPreviewYAnimator.interpolator = interpolator
        animators.add(tagsPreviewYAnimator)

        return animators
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        lytTagsPreview.setTagsToEdit(tagsOnEntry, this)

        val showTagsPreview = this.forceShowTagsPreview || tagsOnEntry.size > 0

        lytTagsPreview.visibility = if(showTagsPreview) View.VISIBLE else View.GONE
        if(fabEditEntryTags.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditEntryTags.visibility = if (showTagsPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread(showContentOnboarding: Boolean? = null) {
        val calculatedShowContentOnboarding = if(showContentOnboarding == null) lytContentWebView.visibility != View.VISIBLE else showContentOnboarding
        setOnboardingTextVisibilityOnUIThread(calculatedShowContentOnboarding)
        setFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setFloatingActionButtonVisibilityOnUIThread() {
        // when user comes to EditEntryDialog, don't show floatingActionMenu till some content has been entered. She/he should focus on the content
        val hasUserEverEnteredSomeContent = dataManager.localSettings.didShowAddItemPropertiesHelp || contentToEdit.isNullOrBlank() == false

        floatingActionMenu.setVisibilityOnUIThread(wbvwContent.isInFullscreenMode || isEditingContent() || lytContextHelpReaderView.visibility == View.VISIBLE,
                hasUserEverEnteredSomeContent)
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

        if(localSettings.didShowItemInformationFullscreenGesturesHelp == false) {
            dialogService.showConfirmationDialog(getString(R.string.context_help_item_content_fullscreen_gestures), config = ConfirmationDialogConfig(false)) {
                runOnUiThread {
                    wbvwContent.leaveFullscreenModeAndWaitTillLeft { // leave fullscreen otherwise a lot of unwanted behaviour occurs
                        userConfirmedHelpOnUIThread()
                    }
                }
            }

            localSettings.didShowItemInformationFullscreenGesturesHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
        else {
            wbvwContent.leaveFullscreenModeAndWaitTillLeft {// leave fullscreen otherwise a lot of unwanted behaviour occurs
                userConfirmedHelpOnUIThread()
            }
        }
    }


    private fun leaveFullscreenMode() {
        lytEntryFieldsPreview.visibility = View.VISIBLE
        txtEntryContentLabel.visibility = View.VISIBLE
        appBarLayout.visibility = View.VISIBLE
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun enterFullscreenMode() {
        lytEntryFieldsPreview.visibility = View.GONE
        txtEntryContentLabel.visibility = View.GONE
        lytOnboardingText.visibility = View.GONE
        appBarLayout.visibility = View.GONE
        setFloatingActionButtonVisibilityOnUIThread()

        content_layout_root.invalidate()

        mayShowEntryInformationFullscreenHelpOnUIThread()
    }

    private fun mayShowEntryInformationFullscreenHelpOnUIThread() {
        val localSettings = entryService.dataManager.localSettings

        if(localSettings.didShowItemInformationFullscreenHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpFullscreenMode, R.string.context_help_item_content_fullscreen)

            localSettings.didShowItemInformationFullscreenHelp = true
            entryService.dataManager.localSettingsUpdated()
        }
    }


    override fun onPause() {
        unregisterEventBusListener()

        if(wbvwContent.isInFullscreenMode) {
            wbvwContent.leaveFullscreenModeAndWaitTillLeft {  }
        }

        contentEditor.hideKeyboard()
        lytAbstractPreview.stopEditing()

        super.onPause()
    }

    override fun onDestroy() {
        pauseWebView()

        parameterHolder.clearActivityResults(EditReferenceActivity.ResultId)

        super.onDestroy()
    }

    private fun pauseWebView() {
        // to prevent that a video keeps on playing in WebView when navigating away from ViewEntryActivity
        // see https://stackoverflow.com/a/6230902
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause")
                    .invoke(wbvwContent)

        } catch(ignored: Exception) { }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(openUrlOptionsView.handlesTouch(event)) {
            return true
        }
        else if(floatingActionMenu.handlesTouch(event)) { // close menu when menu is opened and touch is outside floatingActionMenuButton
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        if(openUrlOptionsView.handlesBackButtonPress()) {
            return
        }
        else if(floatingActionMenu.handlesBackButtonPress()) {
            return
        }
        else if(isInEditContentMode) {
            if(item == null || item?.isPersisted() == true || this.haveAllFieldsBeenCleared() == false) {
                leaveEditContentView()
                return
            }
        }
        else if(lytReferencePreview.handlesBackButtonPress()) {
            return
        }
        else if(lytTagsPreview.handlesBackButtonPress()) {
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(isInEditContentMode) {
            createEditHtmlOptionsMenu(menu)
        }
        else {
            createViewHtmlOptionsMenu(menu)
        }

        return true
    }

    private fun createEditHtmlOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.activity_edit_entry_edit_content_menu, menu)
    }

    private fun createViewHtmlOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.activity_edit_entry_menu, menu)

        mnSaveEntry = menu.findItem(R.id.mnSaveEntry)
        readLaterArticle?.let { mnSaveEntry?.setIcon(R.drawable.ic_tab_items) }

        mnDeleteExistingEntry = menu.findItem(R.id.mnDeleteExistingEntry)
        mnDeleteExistingEntry?.isVisible = item?.isPersisted() == true

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        mnToggleReaderMode?.isVisible = itemExtractionResult?.couldExtractContent == true || readLaterArticle?.itemExtractionResult?.couldExtractContent == true /*&& webSiteHtml != null*/ // show mnToggleReaderMode only if previously original web site was shown
        setReaderModeActionStateOnUIThread()

        mnSaveEntryExtractionResultForLaterReading = menu.findItem(R.id.mnSaveEntryExtractionResultForLaterReading)
        mnSaveEntryExtractionResultForLaterReading?.isVisible = itemExtractionResult != null

        mnDeleteReadLaterArticle = menu.findItem(R.id.mnDeleteReadLaterArticle)
        mnDeleteReadLaterArticle?.isVisible = readLaterArticle != null

        mnShareEntry = menu.findItem(R.id.mnShareEntry)
        mnShareEntry?.isVisible = sourceToEdit?.url.isNullOrBlank() == false

        setMenuSaveEntryVisibleStateOnUIThread()

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }
    }

    private fun setReaderModeActionStateOnUIThread() {
        mnToggleReaderMode?.let { mnToggleReaderMode ->
            if(mnToggleReaderMode.isVisible == true) {
                if(isInReaderMode) {
                    mnToggleReaderMode.title = getString(R.string.action_website_view)
                    mnToggleReaderMode.setIcon(R.drawable.ic_reader_mode_disabled)
                }
                else {
                    mnToggleReaderMode.title = getString(R.string.action_reader_view)
                    mnToggleReaderMode.setIcon(R.drawable.ic_reader_mode)
                }

                toolbarUtil.updateMenuItemView(mnToggleReaderMode)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                if(isInEditContentMode) {
                    leaveEditContentView()
                }
                else {
                    askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                }
                return true
            }
            R.id.mnSaveEntry -> {
                saveEntryAndCloseDialog()
                return true
            }
            R.id.mnDeleteExistingEntry -> {
                askIfShouldDeleteExistingEntryAndCloseDialog()
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
            R.id.mnApplyHtmlChanges -> {
                appliedChangesToContent()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun toggleReaderMode() {
        isInReaderMode = !isInReaderMode

        if(isInReaderMode) {
            val extractionResult = itemExtractionResult ?: readLaterArticle?.itemExtractionResult
            contentToEdit = extractionResult?.item?.content ?: ""
        }
        else {
            contentToEdit = webSiteHtml ?: ""
        }

        setContentPreviewOnUIThread()
        invalidateOptionsMenu()

        checkIsShowingReaderViewHelp()
    }

    private fun checkIsShowingReaderViewHelp() {
        val dataManager = entryService.dataManager

        if (dataManager.localSettings.didShowReaderViewHelp == false) {
            dataManager.localSettings.didShowReaderViewHelp = true
            dataManager.localSettingsUpdated()

            if(lytContextHelpReaderView.visibility == View.VISIBLE) {
                contextHelpUtil.animateHideContextHelp(lytContextHelpReaderView) {
                    setFloatingActionButtonVisibilityOnUIThread()
                }
            }
        }
    }

    private fun showShareEntryPopupMenu() {
        val overflowMenuButton = getOverflowMenuButton()
        if(overflowMenuButton == null) {
            return
        }

        val popup = PopupMenu(this, overflowMenuButton)

        popup.menuInflater.inflate(R.menu.share_entry_menu, popup.menu)

        val reference = sourceToEdit
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
        sourceToEdit?.let { reference ->
            presenter.shareReferenceUrl(reference)
        }
    }

    private fun shareEntryContent() {
        item?.let { entry ->
            presenter.shareEntry(entry, entry.source, entry.source?.series)
        }

        readLaterArticle?.itemExtractionResult?.let { extractionResult ->
            presenter.shareEntry(extractionResult.item, extractionResult.source, extractionResult.series)
        }

        itemExtractionResult?.let { extractionResult ->
            presenter.shareEntry(extractionResult.item, extractionResult.source, extractionResult.series)
        }
    }


    private fun saveEntryAndCloseDialog() {
        mnSaveEntry?.isEnabled = false // disable to that save cannot be pressed a second time
        mnSaveEntryExtractionResultForLaterReading?.isEnabled = false
        unregisterEventBusListener()

        saveEntryAsync { successful ->
            if(successful) {
                mayShowSavedReadLaterArticleHelpAndCloseDialog()
            }
            else {
                mnSaveEntry?.isEnabled = true
                mnSaveEntryExtractionResultForLaterReading?.isEnabled = true
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveEntryAsync(callback: (Boolean) -> Unit) {
        val content = contentToEdit ?: ""
        val abstract = abstractToEdit ?: ""

        item?.let { entry ->
            updateEntry(entry, content, abstract)
            presenter.saveEntryAsync(entry, sourceToEdit, sourceToEdit?.series, tags = tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntry = true, savedItem = entry))
                }
                callback(successful)
            }
        }

        itemExtractionResult?.let { extractionResult ->
            // TODO: save extracted content when in reader mode and webSiteHtml when not in reader mode
            // TODO: contentToEdit show now always contain the correct value depending on is or is not in reader mode, doesn't it?

            updateEntry(extractionResult.item, content, abstract)
            presenter.saveEntryAsync(extractionResult.item, sourceToEdit, extractionResult.series, tagsOnEntry) { successful ->
                if(successful) {
                    setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedItem = extractionResult.item))
                }
                callback(successful)
            }
        }

        readLaterArticle?.let { readLaterArticle ->
            val extractionResult = readLaterArticle.itemExtractionResult
            updateEntry(extractionResult.item, content, abstract)

            presenter.saveEntryAsync(extractionResult.item, sourceToEdit, extractionResult.series, tagsOnEntry) { successful ->
                if(successful) {
                    readLaterArticleService.delete(readLaterArticle)
                    setActivityResult(EditEntryActivityResult(didSaveReadLaterArticle = true, savedItem = extractionResult.item))
                }
                callback(successful)
            }
        }
    }

    private fun mayShowSavedReadLaterArticleHelpAndCloseDialog() {
        mayShowSavedReadLaterArticleHelp {
            runOnUiThread {
                closeDialog()
            }
        }
    }

    private fun mayShowSavedReadLaterArticleHelp(callback: () -> Unit) {
        val localSettings = entryService.dataManager.localSettings

        if(readLaterArticle != null && localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp == false) {
            localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp = true
            entryService.dataManager.localSettingsUpdated()

            val helpText = getText(R.string.context_help_saved_read_later_article_is_now_in_items).toString()
            dialogService.showConfirmationDialog(contextHelpUtil.stringUtil.getSpannedFromHtml(helpText), config = ConfirmationDialogConfig(false)) {
                callback()
            }
        }
        else {
            callback()
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

        itemExtractionResult?.let { extractionResult ->
            updateEntry(extractionResult.item, content, abstract)
            extractionResult.source = sourceToEdit
            extractionResult.tags = tagsOnEntry

            presenter.saveEntryExtractionResultForLaterReading(extractionResult)
            setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedItem = extractionResult.item))
            callback(true)
        }

        if(itemExtractionResult == null) {
            callback(false)
        }
    }


    private fun askIfShouldDeleteExistingEntryAndCloseDialog() {
        item?.let { entry ->
            dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_delete_item, entry.preview)) { selectedButton ->
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    mnDeleteExistingEntry?.isEnabled = false
                    unregisterEventBusListener()

                    deleteEntityService.deleteEntry(entry)
                    closeDialog()
                }
            }
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

    private fun updateEntry(item: Item, content: String, abstract: String) {
        item.content = content
        item.summary = abstract

        if(changedFields.contains(ItemField.SourceTitle)) {
            sourceToEdit?.title = lytReferencePreview.getEditedValue() ?: ""
        }
        if(sourceToEdit?.isPersisted() == false && lytReferencePreview.getEditedValue().isNullOrBlank()) {
            sourceToEdit = null
            readLaterArticle?.itemExtractionResult?.series = null
            itemExtractionResult?.series = null
        }

        if(sourceToEdit != originalSource) {
            readLaterArticle?.itemExtractionResult?.series = null
            itemExtractionResult?.series = null
        }

        if(changedFields.contains(ItemField.Tags)) {
            tagsOnEntry.clear()
            tagsOnEntry.addAll(lytTagsPreview.applyChangesAndGetTags())
        }
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(changedFields.size > 0) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_save))
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_item_contains_unsaved_changes), config = config) { selectedButton ->
            runOnUiThread {
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    saveEntryAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                    closeDialog()
                }
            }
        }
    }

    private fun closeDialog() {
        finish()
    }


    private fun showParameters(parameters: EditEntryActivityParameters?) {
        if(parameters != null) {
            parameters.item?.let { editEntry(it) }

            parameters.readLaterArticle?.let {
                isInReaderMode = it.itemExtractionResult.couldExtractContent
                editReadLaterArticle(it)
            }

            parameters.itemExtractionResult?.let {
                isInReaderMode = it.couldExtractContent
                editEntryExtractionResult(it)
            }

            if(parameters.createEntry) {
                createEntry()
            }
        }
    }

    private fun createEntry(editContent: Boolean = true) {
        editEntry(Item(""))

        if(editContent) {
            editContent() // go directly to edit content dialog, there's absolutely nothing to see on this almost empty screen
        }
    }

    private fun editEntry(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            editEntry(entry)
        }
    }

    private fun editEntry(item: Item) {
        this.item = item

        mnDeleteExistingEntry?.isVisible = item.isPersisted()

        editEntry(item, item.source, item.tags)
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            editReadLaterArticle(readLaterArticle)
        }
    }

    private fun editReadLaterArticle(readLaterArticle: ReadLaterArticle, updateContentPreview: Boolean = true) {
        this.readLaterArticle = readLaterArticle

        mnSaveEntry?.setIcon(R.drawable.ic_tab_items)
        editEntry(readLaterArticle.itemExtractionResult.item, readLaterArticle.itemExtractionResult.source, readLaterArticle.itemExtractionResult.tags, updateContentPreview)
    }

    private fun editEntryExtractionResult(extractionResult: ItemExtractionResult, updateContentPreview: Boolean = true) {
        this.itemExtractionResult = extractionResult

        editEntry(itemExtractionResult?.item, itemExtractionResult?.source, itemExtractionResult?.tags, updateContentPreview)
    }

    private fun editEntry(item: Item?, source: Source?, tags: MutableCollection<Tag>?, updateContentPreview: Boolean = true) {
        originalContent = item?.content
        originalTags = tags
        originalSource = source
        originalTitleAbstract = item?.summary

        contentToEdit = item?.content
        abstractToEdit = item?.summary
        sourceToEdit = source

        if(abstractToEdit.isNullOrBlank() == false) { this.forceShowAbstractPreview = true } // forcing that once it has been shown it doesn't get hidden anymore

        source?.let { this.forceShowReferencePreview = true } // forcing that once it has been shown it doesn't get hidden anymore
        lytReferencePreview.setOriginalSourceToEdit(sourceToEdit, getCurrentSeries(), this) { setSourceToEdit(it) }

        tags?.forEach { tag ->
            if(tagsOnEntry.contains(tag) == false) { // to avoid have a tag twice we really have to check each single tag
                tagsOnEntry.add(tag)
            }
        }

        if(tags?.size ?: 0 > 0) {
            forceShowTagsPreview = true
        }

        updateDisplayedValuesOnUIThread(source, updateContentPreview)
    }

    private fun updateDisplayedValuesOnUIThread(source: Source? = sourceToEdit, updateContentPreview: Boolean = true) {
        if(updateContentPreview) {
            setContentPreviewOnUIThread(source)
        }

        setTagsOnEntryPreviewOnUIThread()

        setReferencePreviewOnUIThread()

        setAbstractPreviewOnUIThread()
    }

    private fun restoreReference(referenceId: String?) {
        if(referenceId != null) {
            sourceToEdit = referenceService.retrieve(referenceId)
        }
        else {
            sourceToEdit = null
        }

        runOnUiThread {
            lytReferencePreview.setOriginalSourceToEdit(sourceToEdit, getCurrentSeries(), this) { setSourceToEdit(it) }
            setReferencePreviewOnUIThread()
        }
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


    private fun userClickedOnUrl(url: String) {
        openUrlOptionsView.showMenuCenter(txtEntryContentLabel) { selectedOption ->
            when(selectedOption) {
                OpenUrlOptionsView.OpenUrlOption.OpenInNewActivity -> executeUserClickedUrlAction { showUrlInNewActivity(url) }
                OpenUrlOptionsView.OpenUrlOption.OpenWithOtherApp -> executeUserClickedUrlAction { openUrlWithOtherApp(url) }
            }
        }
    }

    private fun executeUserClickedUrlAction(action: () -> Unit) {
        wbvwContent.leaveFullscreenModeAndWaitTillLeft {
            action()
        }
    }

    private fun showUrlInNewActivity(url: String) {
        extractArticleHandler.extractAndShowArticleUserDidNotSeeBefore(url)
    }

    private fun openUrlWithOtherApp(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch(e: Exception) { log.error("Could not open url $url with other app", e) }
    }


    private fun mayRegisterEventBusListener() {
        if(item?.isPersisted() ?: false && eventBusListener == null) {
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

    private fun warnEntryHasBeenEdited(item: Item) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_item_alert_message_item_has_been_edited))
        }
    }

    private fun updateDisplayedValues() {
        runOnUiThread { updateDisplayedValuesOnUIThread() }
    }

    inner class EventBusListener {

        @Handler
        fun entryChanged(change: EntryChanged) {
            if(change.entity.id == item?.id && change.isDependentChange == false) {
                if(change.source == EntityChangeSource.Synchronization) {
                    warnEntryHasBeenEdited(change.entity)
                }
                else {
                    updateDisplayedValues()
                }
            }
        }
    }

}
