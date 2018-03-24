package net.dankito.deepthought.android.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.webkit.*
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.activity_edit_item.*
import kotlinx.android.synthetic.main.view_floating_action_button_item_fields.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditItemActivityResult
import net.dankito.deepthought.android.activities.arguments.EditSourceActivityResult
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.hideKeyboardDelayed
import net.dankito.deepthought.android.views.*
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.fields.ItemField
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditItemPresenter
import net.dankito.filechooserdialog.service.IPermissionsService
import net.dankito.filechooserdialog.service.PermissionsService
import net.dankito.richtexteditor.android.animation.ShowHideViewAnimator
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ItemService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


abstract class EditItemActivityBase : BaseActivity() {

    companion object {
        private const val CHANGED_FIELDS_INTENT_EXTRA_NAME = "CHANGED_FIELDS"

        private const val FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_TAGS_PREVIEW"
        private const val FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SOURCE_PREVIEW"
        private const val FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SUMMARY_PREVIEW"
        private const val FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_FILES_PREVIEW"

        private const val IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME = "IS_IN_EDIT_CONTENT_MODE"
        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val EDIT_CONTENT_HTML_INTENT_EXTRA_NAME = "EDIT_CONTENT_HTML"

        const val ResultId = "EDIT_ITEM_ACTIVITY_RESULT"

        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"

        private const val ShowHideEditContentViewAnimationDurationMillis = 500L

        private const val ShowHideEditTagsAnimationDurationMillis = 250L

        private val log = LoggerFactory.getLogger(EditItemActivityBase::class.java)
    }


    @Inject
    protected lateinit var itemService: ItemService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var itemPersister: ItemPersister

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


    private var originalContent: String? = null

    private var originalTags: MutableCollection<Tag>? = null


    protected lateinit var itemToSave: Item

    protected var contentToEdit: String? = null

    protected val tagsOnItem: MutableList<Tag> = ArrayList()

    private val changedFields = HashSet<ItemField>()

    private var forceShowTagsPreview = false

    private var forceShowSourcePreview = false

    private var forceShowSummaryPreview = false

    private var forceShowFilesPreview = false


    protected val presenter: EditItemPresenter

    protected var isInEditContentMode = false

    protected var isInReaderMode = false

    protected var webSiteHtml: String? = null

    protected var isLoadingUrl = false

    protected var isEditingTagsOnItem = false


    protected val contextHelpUtil = ContextHelpUtil()

    private val toolbarUtil = ToolbarUtil()

    private val openUrlOptionsView = OpenUrlOptionsView()

    private val permissionsManager: IPermissionsService

    private lateinit var editHtmlView: EditHtmlView

    private val animator = ShowHideViewAnimator()

    protected var mnSaveItem: MenuItem? = null

    protected var mnDeleteExistingItem: MenuItem? = null

    protected var mnToggleReaderMode: MenuItem? = null

    protected var mnSaveItemExtractionResultForLaterReading: MenuItem? = null

    protected var mnShareItemSourceUrl: MenuItem? = null

    protected var mnShareItemContent: MenuItem? = null

    private lateinit var floatingActionMenu: EditItemActivityFloatingActionMenuButton


    private val dataManager: DataManager


    protected abstract fun showParameters(parameters: EditItemActivityParameters)

    protected abstract fun restoreEntity(savedInstanceState: Bundle)

    protected abstract fun saveState(outState: Bundle)


    init {
        AppComponent.component.inject(this)

        dataManager = itemService.dataManager

        presenter = EditItemPresenter(itemPersister, readLaterArticleService, clipboardService, router)

        permissionsManager = PermissionsService(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditItemActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        if(savedInstanceState == null) {
            (getParameters() as? EditItemActivityParameters)?.let { showParameters(it) }
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val itemFields = ItemField.values()
        savedInstanceState.getIntArray(CHANGED_FIELDS_INTENT_EXTRA_NAME)?.forEach { ordinal ->
            if(ordinal < itemFields.size) {
                changedFields.add(itemFields[ordinal])
            }
        }

        this.forceShowTagsPreview = savedInstanceState.getBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowSourcePreview = savedInstanceState.getBoolean(FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowSummaryPreview = savedInstanceState.getBoolean(FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowFilesPreview = savedInstanceState.getBoolean(FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME, false)

        this.isInEditContentMode = savedInstanceState.getBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, false)
        this.isInReaderMode = savedInstanceState.getBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, false)

        restoreEntity(savedInstanceState)

        restoreStateFromDisk(savedInstanceState, CONTENT_INTENT_EXTRA_NAME, String::class.java)?.let { content ->
            contentToEdit = content
            setContentPreviewOnUIThread()
        }

        wbvwContent.restoreInstanceState(savedInstanceState)

        floatingActionMenu.restoreInstanceState(savedInstanceState)


        restoreStateFromDisk(savedInstanceState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, String::class.java)?.let {
            editHtmlView.setHtml(it, lytSourcePreview.source?.url)
        }

        if(isInEditContentMode) {
            editContent()
        }

        setMenuSaveItemVisibleStateOnUIThread()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            saveState(outState)

            outState.putIntArray(CHANGED_FIELDS_INTENT_EXTRA_NAME, changedFields.map { it.ordinal }.toIntArray())

            outState.putBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, forceShowTagsPreview)
            outState.putBoolean(FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME, forceShowSourcePreview)
            outState.putBoolean(FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME, forceShowSummaryPreview)
            outState.putBoolean(FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME, forceShowFilesPreview)

            outState.putBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, isInEditContentMode)
            outState.putBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, isInReaderMode)

            if(contentToEdit != originalContent) {
                serializeStateToDiskIfNotNull(outState, CONTENT_INTENT_EXTRA_NAME, contentToEdit) // application crashes if objects put into bundle are too large (> 1 MB) for Android
            }

            if(isInEditContentMode) {
                serializeStateToDiskIfNotNull(outState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, editHtmlView.getHtml()) // application crashes if objects put into bundle are too large (> 1 MB) for Android
            }

            wbvwContent.onSaveInstanceState(outState)

            floatingActionMenu.saveInstanceState(outState)
        }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_item)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }

        lytSummaryPreview.setFieldNameOnUiThread(R.string.activity_edit_item_title_summary_label) { didSummaryChange -> summaryChanged(didSummaryChange) }
        lytSummaryPreview.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                summaryChanged(lytSummaryPreview.didValueChange)
            }
        }

        lytSourcePreview.didValueChangeListener = { didSourceTitleChange -> updateItemFieldChangedOnUIThread(ItemField.SourceTitle, didSourceTitleChange) }
        lytSourcePreview.didSecondaryInformationValueChangeListener = { updateItemFieldChangedOnUIThread(ItemField.Indication, it) }

        lytTagsPreview.didValueChangeListener = { didTagsChange ->
            itemPropertySet()
            updateItemFieldChangedOnUIThread(ItemField.Tags, didTagsChange)
        }
        lytTagsPreview.fieldValueFocusChangedListener = { hasFocus -> tagsPreviewFocusChanged(hasFocus) }
        lytTagsPreview.saveChangesListener = {
            if(mnSaveItem?.isEnabled == true) {
                saveItemAndCloseDialog()
            }
        }

        lytFilesPreview.didValueChangeListener = { didFilesChange ->
            itemPropertySet()
            updateItemFieldChangedOnUIThread(ItemField.Files, didFilesChange)
        }

        wbvwContent?.requestFocus() // avoid that lytSummaryPreview gets focus and keyboard therefore gets show on activity start

        floatingActionMenu = EditItemActivityFloatingActionMenuButton(findViewById(R.id.floatingActionMenu) as FloatingActionMenu, { addTagsToItem() },
                { addSourceToItem() }, { addSummaryToItem() }, { addFilesToItem() } )

        setupItemContentView()

        setupItemContentEditor()
    }

    private fun setupItemContentEditor() {
        editHtmlView = EditHtmlView(this)
        editHtmlView.setupHtmlEditor(lytEditContent)

        editHtmlView.setHtmlChangedCallback { didChange ->
            updateItemFieldChanged(ItemField.Content,didChange)
        }
    }

    private fun addTagsToItem() {
        editTagsOnItem()

        forceShowTagsPreview = true
        setTagsOnItemPreviewOnUIThread()
    }

    private fun addSourceToItem() {
        editSource()

        forceShowSourcePreview = true
        setSourcePreviewOnUIThread()
    }

    private fun addSummaryToItem() {
        forceShowSummaryPreview = true
        updateShowSummaryPreviewOnUiThread()

        lytSummaryPreview.startEditing()
    }

    private fun addFilesToItem() {
        forceShowFilesPreview = true
        setFilesPreviewOnUIThread()

        lytFilesPreview.selectFilesToAdd()
    }

    private fun setupItemContentView() {
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
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request?.url?.toString()?.let { url ->
                userClickedOnUrl(url)
            }

            return true
        }

        @Suppress("OverridingDeprecatedMember")
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

            @Suppress("DEPRECATION")
            wbvwContent.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        }
    }

    private fun webPageCompletelyLoaded(webView: WebView) {
        runOnUiThread { webPageCompletelyLoadedOnUiThread(webView) }
    }

    private fun webPageCompletelyLoadedOnUiThread(webView: WebView) {
        if(getItemExtractionResult() == null) { // an item
            urlLoadedNow()
        }
        // if ItemExtractionResult's item content hasn't been extracted yet, wait till WebView is loaded and extract item content then
        else if(getItemExtractionResult() != null && isInReaderMode == false &&
                webView.url != null && webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
            webView.loadUrl("javascript:$GetHtmlCodeFromWebViewJavaScriptInterfaceName.finishedLoadingSite" +
                    "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }
    }

    private fun siteFinishedLoading(url: String, html: String) {
        urlLoadedNow()

        // now try to extract item content from WebView's html
        val extractionResult = getItemExtractionResult()
        if(extractionResult != null && isInReaderMode == false) {
            webSiteHtml = html
            contentToEdit = html

            if(extractionResult.couldExtractContent == false) {
                articleExtractorManager.extractArticleUserDidSeeBefore(extractionResult, html, url)

                if(extractionResult.couldExtractContent) {
                    runOnUiThread { extractedContentOnUiThread(extractionResult) }
                }
            }
        }
    }

    protected open fun getItemExtractionResult(): ItemExtractionResult? {
        return null
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

        // updates source and summary, but avoids that extracted content gets shown (this is important according to our
        // lawyer, user must click on toggleReaderMode menu first)
        editItem(extractionResult.item, extractionResult.source, extractionResult.series, extractionResult.tags, extractionResult.files, false)

        mayShowReaderViewHelp()
    }

    private fun mayShowReaderViewHelp() {
        val localSettings = itemService.dataManager.localSettings
        if(localSettings.didShowReaderViewHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpReaderView, R.string.context_help_reader_view) {
                setFloatingActionButtonVisibilityOnUIThread()
                localSettings.didShowReaderViewHelp = true
                itemService.dataManager.localSettingsUpdated()
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditSourceActivity.ResultId) as? EditSourceActivityResult)?.let { result ->
            lytSourcePreview.editingSourceDone(result)
        }

        setContentPreviewOnUIThread()

        wbvwContent.activityResumed()

        lytSourcePreview.viewBecomesVisible()
        lytTagsPreview.viewBecomesVisible()
        lytFilesPreview.viewBecomesVisible()
    }


    protected fun editContent() {
        if(isLoadingUrl) { // while WebView is still loading contentToEdit is not set yet (contentToEdit gets set when loading finishes)
            return
        }

        contentToEdit?.let { content ->
            editHtmlView.setHtml(content, lytSourcePreview.source?.url)

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
                .ofFloat(lytItemFieldsPreview, View.Y, lytItemFieldsPreview.top.toFloat(), -1 * lytItemFieldsPreview.measuredHeight.toFloat())
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
        contentEditor.retrieveCurrentHtmlAsync { html -> // update contentToEdit as paste or Samsung's Swipe keyboard doesn't raise changed event -> fetch value before saving
            contentToEdit = html

            runOnUiThread {
                leaveEditContentView()

                updateItemFieldChangedOnUIThread(ItemField.Content, originalContent != contentToEdit)
                setContentPreviewOnUIThread()
            }
        }
    }

    private fun leaveEditContentView() {
        contentEditor.hideKeyboardDelayed(250) // for Samsungs we need a delay (again an exception of Samsung devices, i really dislike them)

        animator.playShowAnimation(lytItemFieldsPreview)
        lytEditContent.visibility = View.GONE
        lytViewContent.visibility = View.VISIBLE
        setFloatingActionButtonVisibilityOnUIThread()

        isInEditContentMode = false

        invalidateOptionsMenu()
    }

    private fun summaryChanged(didSummaryChange: Boolean) {
        itemPropertySet()

        updateItemFieldChanged(ItemField.TitleOrSummary, didSummaryChange)
    }

    private fun sourceChanged(source: Source?) {
        updateItemFieldChangedOnUIThread(ItemField.Source, source != lytSourcePreview.originalSource)

        itemPropertySet() // TODO: still senseful?

        updateShowMenuItemShareItem()
    }

    private fun editSource() {
        lytSourcePreview.visibility = View.VISIBLE
        lytSourcePreview.startEditing()
    }

    private fun editTagsOnItem() {
        lytTagsPreview.visibility = View.VISIBLE
        lytTagsPreview.startEditing()
    }

    private fun updateItemFieldChanged(field: ItemField, didChange: Boolean) {
        runOnUiThread {
            updateItemFieldChangedOnUIThread(field, didChange)
        }
    }

    private fun updateItemFieldChangedOnUIThread(field: ItemField, didChange: Boolean) {
        if(didChange) {
            changedFields.add(field)
        }
        else {
            changedFields.remove(field)
        }

        setMenuSaveItemVisibleStateOnUIThread()
    }

    private fun setMenuSaveItemVisibleStateOnUIThread() {
        mnSaveItem?.isVisible = isEntitySavable()
    }

    protected open fun isEntitySavable(): Boolean {
        return true // ItemExtractionResult and ReadLaterArticle always can be saved, only EditItemActivity has to set this value
    }

    private fun itemPropertySet() {
        val localSettings = itemService.dataManager.localSettings

        if(localSettings.didShowAddItemPropertiesHelp == false && contentToEdit.isNullOrBlank() == false) {
            localSettings.didShowAddItemPropertiesHelp = true
            itemService.dataManager.localSettingsUpdated()
        }
    }


    private fun setContentPreviewOnUIThread() {
        setContentPreviewOnUIThread(lytSourcePreview.source)
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
        else if(url != null && getItemExtractionResult() != null) { // then load url (but don't show it for an Item)
            clearWebViewItem()
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
                (getItemExtractionResult() == null || (isInReaderMode && getItemExtractionResult()?.couldExtractContent == true) ) // getItemExtractionResult() == null -> it's an Item
    }

    private fun showContentInWebView(contentParam: String?, url: String?) {
        var content = contentParam

        // TODO: remove and set font in css
        if(content?.startsWith("<html") == false && content.startsWith("<body") == false && content.startsWith("<!doctype") == false) {
            // this is the same style as in Android app's platform_style.css
            content = "<html><head><style type=\"text/css\">\n" +
                        "body {\n" +
                        "    font-family: serif;\n" +
                        "    font-size: 18px;" +
                        "}\n" +
                        "h1, h2, h3, h4, h5, h6 {\n" +
                        "    font-family: Roboto, sans-serif;\n" +
                        "    color: #7f7f7f;\n" +
                        "}\n" +
                        "</style></head><body>$content</body></html>"
        }

        clearWebViewItem() // clear WebView
        if(url != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) { // loading html with loadDataWithBaseURL() didn't work for me on 4.1 (API 16), just displayed HTML code
            wbvwContent.loadDataWithBaseURL(url, content, "text/html; charset=UTF-8", "utf-8", null)
        }
        else {
            wbvwContent.loadData(content, "text/html; charset=UTF-8", "utf-8")
        }
    }

    private fun clearWebViewItem() {
        if(Build.VERSION.SDK_INT < 18) {
            @Suppress("DEPRECATION")
            wbvwContent.clearView()
        }
        else {
            wbvwContent.loadUrl("about:blank")
        }
    }

    private fun setOnboardingTextVisibilityOnUIThread(showContentOnboarding: Boolean? = null) {
        val showOnboardingForItemProperties = shouldShowOnboardingForItemProperties()
        if(showContentOnboarding == true || showOnboardingForItemProperties) {
            lytOnboardingText.visibility = View.VISIBLE
            lytContentWebViewAndOnboardingText.setOnClickListener { editContent() } // only enable editing content by clicking on lytContentWebViewAndOnboardingText when showing onboarding text

            val onboardingTextId = if(showContentOnboarding == true) R.string.activity_edit_item_edit_content_onboarding_text else R.string.activity_edit_item_add_item_properties_onboarding_text
            val onboardingText = if(showContentOnboarding == true) getText(onboardingTextId).toString() else getText(onboardingTextId).toString()
            txtOnboardingText.text = contextHelpUtil.stringUtil.getSpannedFromHtml(onboardingText)

            arrowToFloatingActionButton.visibility = if(showContentOnboarding != true && showOnboardingForItemProperties) View.VISIBLE else View.GONE
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

    private fun shouldShowOnboardingForItemProperties(): Boolean {
        return itemService.dataManager.localSettings.didShowAddItemPropertiesHelp == false &&
                lytTagsPreview.visibility == View.GONE && lytSourcePreview.visibility == View.GONE &&
                lytSummaryPreview.visibility == View.GONE && lytFilesPreview.visibility == View.GONE
    }


    private fun setSummaryPreviewOnUIThread(summaryToEdit: String) {
        val alsoShowTitleInCaption = lytSourcePreview.source?.url == null && summaryToEdit.length < 35 // TODO: shouldn't it be sourceToEdit == null ?
        lytSummaryPreview.setFieldNameOnUiThread(if(alsoShowTitleInCaption) R.string.activity_edit_item_title_summary_label else R.string.activity_edit_item_summary_only_label)

        lytSummaryPreview.setFieldValueOnUiThread(summaryToEdit.getPlainTextForHtml())

        if(summaryToEdit.isBlank()) {
//            lytSummaryPreview.setOnboardingTextOnUiThread(R.string.activity_edit_item_summary_onboarding_text)
        }

        updateShowSummaryPreviewOnUiThread()

        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun updateShowSummaryPreviewOnUiThread() {
        val showSummaryPreview = (this.forceShowSummaryPreview || lytSummaryPreview.getCurrentFieldValue().isEmpty() == false) && isEditingTagsOnItem == false

        lytSummaryPreview.visibility = if(showSummaryPreview) View.VISIBLE else View.GONE
        if(fabEditItemSummary.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemSummary.visibility = if(showSummaryPreview) View.GONE else View.VISIBLE
        }
    }

    private fun setSourcePreviewOnUIThread() {
        updateShowSourcePreviewOnUiThread()

        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()

        updateShowMenuItemShareItem()
    }

    private fun updateShowSourcePreviewOnUiThread() {
        val showSourcePreview = (this.forceShowSourcePreview || lytSourcePreview.source != null) && isEditingTagsOnItem == false

        lytSourcePreview.visibility = if(showSourcePreview) View.VISIBLE else View.GONE
        if(fabEditItemSource.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemSource.visibility = if(showSourcePreview) View.GONE else View.VISIBLE
        }
    }

    private fun setFilesPreviewOnUIThread() {
        val showFilesPreview = (this.forceShowFilesPreview || lytFilesPreview.getEditedFiles().size > 0) && isEditingTagsOnItem == false

        lytFilesPreview.visibility = if(showFilesPreview) View.VISIBLE else View.GONE
        if(fabEditItemFiles.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemFiles.visibility = if(showFilesPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun updateShowMenuItemShareItem() {
        mnShareItemSourceUrl?.isVisible = lytSourcePreview.source?.url.isNullOrBlank() == false
        mnShareItemContent?.isVisible = contentToEdit.isNullOrBlank() == false
    }

    private fun tagsPreviewFocusChanged(hasFocus: Boolean) {
        if(hasFocus) {
            lytTagsPreview.visibility = View.VISIBLE

            if(lytSourcePreview.visibility == View.VISIBLE || lytSummaryPreview.visibility == View.VISIBLE || lytFilesPreview.visibility == View.VISIBLE) {
                lytTagsPreview.executeActionAfterMeasuringHeight {
                    playHideOtherItemFieldsPreviewExceptTagsAnimation()
                }
            }

            isEditingTagsOnItem = true
            setFloatingActionButtonVisibilityOnUIThread()
        }
        else {
            isEditingTagsOnItem = false
            restoreLayoutItemFieldsPreview()
            setFloatingActionButtonVisibilityOnUIThread()
        }
    }

    private fun restoreLayoutItemFieldsPreview() {
        if(lytSourcePreview.measuredHeight > 0) { // only if it has been visible before
            lytSourcePreview.y = lytSourcePreview.top.toFloat()
            lytSourcePreview.visibility = View.VISIBLE
        }

        if(lytSummaryPreview.measuredHeight > 0) {
            lytSummaryPreview.y = lytSummaryPreview.top.toFloat()
            lytSummaryPreview.visibility = View.VISIBLE
        }

        if(lytFilesPreview.measuredHeight > 0) {
            lytFilesPreview.y = lytFilesPreview.top.toFloat()
            lytFilesPreview.visibility = View.VISIBLE
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
        lytSourcePreview.visibility = View.GONE
        lytSummaryPreview.visibility = View.GONE
        lytFilesPreview.visibility = View.GONE
        lytTagsPreview.y = lytTagsPreview.top.toFloat()

        lytItemFieldsPreview.invalidate()
        lytItemFieldsPreview.forceLayout()
        lytItemFieldsPreview.invalidate()
        lytItemFieldsPreview.forceLayout()
    }

    private fun createAnimatorsToHideOtherItemFieldsPreviewExceptTags(): ArrayList<Animator> {
        val animators = ArrayList<Animator>()
        val interpolator = AccelerateInterpolator()

        if(lytSourcePreview.visibility == View.VISIBLE) {
            val sourcePreviewYAnimator = ObjectAnimator
                    .ofFloat(lytSourcePreview, View.Y, lytSourcePreview.top.toFloat(), -1 * lytSourcePreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            sourcePreviewYAnimator.interpolator = interpolator

            animators.add(sourcePreviewYAnimator)
        }

        if(lytSummaryPreview.visibility == View.VISIBLE) {
            val summaryPreviewYAnimator = ObjectAnimator
                    .ofFloat(lytSummaryPreview, View.Y, lytSummaryPreview.top.toFloat(), -1 * lytSummaryPreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            summaryPreviewYAnimator.interpolator = interpolator

            animators.add(summaryPreviewYAnimator)
        }

        if(lytFilesPreview.visibility == View.VISIBLE) {
            val summaryPreviewYAnimator = ObjectAnimator
                    .ofFloat(lytFilesPreview, View.Y, lytFilesPreview.top.toFloat(), -1 * lytFilesPreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            summaryPreviewYAnimator.interpolator = interpolator

            animators.add(summaryPreviewYAnimator)
        }

        val location = IntArray(2)
        lytItemFieldsPreview.getLocationOnScreen(location)

        val tagsPreviewYAnimator = ObjectAnimator
                .ofFloat(lytTagsPreview, View.Y, lytTagsPreview.top.toFloat(), location[1].toFloat())
                .setDuration(ShowHideEditTagsAnimationDurationMillis)
        tagsPreviewYAnimator.interpolator = interpolator
        animators.add(tagsPreviewYAnimator)

        return animators
    }

    private fun setTagsOnItemPreviewOnUIThread() {
        lytTagsPreview.setTagsToEdit(tagsOnItem, this)

        val showTagsPreview = this.forceShowTagsPreview || tagsOnItem.size > 0

        lytTagsPreview.visibility = if(showTagsPreview) View.VISIBLE else View.GONE
        if(fabEditItemTags.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemTags.visibility = if (showTagsPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread(showContentOnboarding: Boolean? = null) {
        val calculatedShowContentOnboarding = if(showContentOnboarding == null) lytContentWebView.visibility != View.VISIBLE else showContentOnboarding
        setOnboardingTextVisibilityOnUIThread(calculatedShowContentOnboarding)
        setFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setFloatingActionButtonVisibilityOnUIThread() {
        val forceHidingFloatingActionButton = wbvwContent.isInFullscreenMode || isEditingContent() || isEditingTagsOnItem || lytContextHelpReaderView.visibility == View.VISIBLE
        // when user comes to EditItemDialog, don't show floatingActionMenu till some content has been entered. She/he should focus on the content
        val hasUserEverEnteredSomeContent = dataManager.localSettings.didShowAddItemPropertiesHelp || contentToEdit.isNullOrBlank() == false

        floatingActionMenu.setVisibilityOnUIThread(forceHidingFloatingActionButton, hasUserEverEnteredSomeContent)
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
                    mayShowItemInformationFullscreenGesturesHelpOnUIThread { presenter.returnToPreviousView() }
                }
                OnSwipeTouchListener.SwipeDirection.Right -> {
                    mayShowItemInformationFullscreenGesturesHelpOnUIThread { editTagsOnItem() }
                }
            }
        }
    }

    private fun mayShowItemInformationFullscreenGesturesHelpOnUIThread(userConfirmedHelpOnUIThread: () -> Unit) {
        val localSettings = itemService.dataManager.localSettings

        if(localSettings.didShowItemInformationFullscreenGesturesHelp == false) {
            dialogService.showConfirmationDialog(getString(R.string.context_help_item_content_fullscreen_gestures), config = ConfirmationDialogConfig(false)) {
                runOnUiThread {
                    wbvwContent.leaveFullscreenModeAndWaitTillLeft { // leave fullscreen otherwise a lot of unwanted behaviour occurs
                        userConfirmedHelpOnUIThread()
                    }
                }
            }

            localSettings.didShowItemInformationFullscreenGesturesHelp = true
            itemService.dataManager.localSettingsUpdated()
        }
        else {
            wbvwContent.leaveFullscreenModeAndWaitTillLeft {// leave fullscreen otherwise a lot of unwanted behaviour occurs
                userConfirmedHelpOnUIThread()
            }
        }
    }


    private fun leaveFullscreenMode() {
        lytItemFieldsPreview.visibility = View.VISIBLE
        txtItemContentLabel.visibility = View.VISIBLE
        appBarLayout.visibility = View.VISIBLE
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun enterFullscreenMode() {
        lytItemFieldsPreview.visibility = View.GONE
        txtItemContentLabel.visibility = View.GONE
        lytOnboardingText.visibility = View.GONE
        appBarLayout.visibility = View.GONE
        setFloatingActionButtonVisibilityOnUIThread()

        content_layout_root.invalidate()

        mayShowItemInformationFullscreenHelpOnUIThread()
    }

    private fun mayShowItemInformationFullscreenHelpOnUIThread() {
        val localSettings = itemService.dataManager.localSettings

        if(localSettings.didShowItemInformationFullscreenHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpFullscreenMode, R.string.context_help_item_content_fullscreen)

            localSettings.didShowItemInformationFullscreenHelp = true
            itemService.dataManager.localSettingsUpdated()
        }
    }


    override fun onPause() {
        lytSourcePreview.viewGetsHidden()
        lytTagsPreview.viewGetsHidden()
        lytFilesPreview.viewGetsHidden()

        wbvwContent.activityPaused()

        contentEditor.hideKeyboard()
        lytSummaryPreview.stopEditing()

        super.onPause()
    }

    override fun onDestroy() {
        pauseWebView()

        parameterHolder.clearActivityResults(EditSourceActivity.ResultId)

        super.onDestroy()
    }

    private fun pauseWebView() {
        // to prevent that a video keeps on playing in WebView when navigating away from EditItemActivityBase
        // see https://stackoverflow.com/a/6230902
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause")
                    .invoke(wbvwContent)

            Class.forName("android.webkit.WebView")
                    .getMethod("destroy") // so that also pod casts are for sure stopped
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
            // TODO: haveAllFieldsBeenCleared() doesn't reflect currently set content!
            if(getItemExtractionResult() != null || itemToSave.isPersisted() == true || haveAllFieldsBeenCleared() == false) { // if creating an item and no value has been set, leave EditItemActivity directly, don't just hide contentEditor (as there's nothing to see)
                leaveEditContentView()
                return
            }
        }
        else if(lytSourcePreview.handlesBackButtonPress()) {
            return
        }
        else if(lytTagsPreview.handlesBackButtonPress()) {
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    protected fun haveAllFieldsBeenCleared(): Boolean {
        return contentToEdit.isNullOrBlank() && tagsOnItem.isEmpty() && lytSourcePreview.source == null
                && lytSummaryPreview.getCurrentFieldValue().isEmpty() && lytFilesPreview.getEditedFiles().size == 0
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
        menuInflater.inflate(R.menu.activity_edit_item_edit_content_menu, menu)
    }

    protected open fun createViewHtmlOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.activity_edit_item_menu, menu)

        mnSaveItem = menu.findItem(R.id.mnSaveItem)

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        setReaderModeActionStateOnUIThread()

        mnSaveItemExtractionResultForLaterReading = menu.findItem(R.id.mnSaveItemExtractionResultForLaterReading)

        mnShareItemSourceUrl = menu.findItem(R.id.mnShareItemSourceUrl)
        mnShareItemContent = menu.findItem(R.id.mnShareItemContent)
        updateShowMenuItemShareItem()

        setMenuSaveItemVisibleStateOnUIThread()

        adjustViewHtmlOptionsMenu(menu) // adjusting icons has to be done before toolbarUtil.setupActionItemsLayout() gets called

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }
    }

    protected open fun adjustViewHtmlOptionsMenu(menu: Menu) {

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
            R.id.mnSaveItem -> {
                saveItemAndCloseDialog()
                return true
            }
            R.id.mnToggleReaderMode -> {
                toggleReaderMode()
                return true
            }
            R.id.mnShareItemSourceUrl -> {
                shareSourceUrl()
                return true
            }
            R.id.mnShareItemContent -> {
                shareItemContent()
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
            contentToEdit = itemToSave.content // TODO: but in this way changes made to contentToEdit get overwritten!
        }
        else {
            contentToEdit = webSiteHtml ?: ""
        }

        setContentPreviewOnUIThread()
        invalidateOptionsMenu()

        checkIsShowingReaderViewHelp()
    }

    private fun checkIsShowingReaderViewHelp() {
        val dataManager = itemService.dataManager

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

    private fun shareSourceUrl() {
        lytSourcePreview.source?.let { source ->
            presenter.shareSourceUrl(source)
        }
    }

    private fun shareItemContent() {
        val currentSource = lytSourcePreview.source

        presenter.shareItem(Item(contentToEdit ?: "", lytSummaryPreview.getCurrentFieldValue()), tagsOnItem,
                Source(lytSourcePreview.getCurrentFieldValue(), currentSource?.url ?: "", currentSource?.publishingDate, currentSource?.subTitle), lytSourcePreview.series)
    }


    private fun saveItemAndCloseDialog() {
        mnSaveItem?.isEnabled = false // disable to that save cannot be pressed a second time
        beforeSavingItem()

        saveItemAsync { successful ->
            if(successful) {
                setActivityResult(EditItemActivityResult(didSaveItem = true, savedItem = itemToSave))
            }
            else {
                mnSaveItem?.isEnabled = true
            }

            savingItemDone(successful)
        }
    }

    protected open fun beforeSavingItem() {

    }

    private fun saveItemAsync(callback: (Boolean) -> Unit) {
        val content = contentToEdit ?: ""
        val summary = lytSummaryPreview.getCurrentFieldValue()
        val editedSource = updateSource()
        val editedSeries = lytSourcePreview.series

        // TODO: save extracted content when in reader mode and webSiteHtml when not in reader mode
        // TODO: contentToEdit show now always contain the correct value depending on is or is not in reader mode, doesn't it?

        updateItem(itemToSave, content, summary)
        presenter.saveItemAsync(itemToSave, editedSource, editedSeries, tagsOnItem, lytFilesPreview.getEditedFiles()) { successful ->
            callback(successful)
        }
    }

    protected open fun savingItemDone(successful: Boolean) {
        if(successful) {
            closeDialog()
        }
    }



    protected fun setActivityResult(result: EditItemActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    protected fun updateItem(item: Item, content: String, summary: String) {
        item.content = content
        item.summary = summary

        if(changedFields.contains(ItemField.Indication)) {
            item.indication = lytSourcePreview.getEditedSecondaryInformation()
        }

        if(changedFields.contains(ItemField.Tags)) {
            tagsOnItem.clear()
            tagsOnItem.addAll(lytTagsPreview.applyChangesAndGetTags())
        }
    }

    protected fun updateSource(): Source? {
        var source = lytSourcePreview.source

        if(changedFields.contains(ItemField.SourceTitle)) {
            source?.title = lytSourcePreview.getEditedValue() ?: ""
        }

        if(source?.isPersisted() == false && lytSourcePreview.getEditedValue().isNullOrBlank()) {
            source = null
            resetSeries() // TODO: is this really necessary as we then pass lytSourcePreview.series to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        if(source != lytSourcePreview.originalSource) {
            resetSeries() // TODO: is this really necessary as we then pass lytSourcePreview.series to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        return source
    }

    protected open fun resetSeries() {

    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(hasUnsavedChanges) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    protected val hasUnsavedChanges: Boolean
        get() {
            return changedFields.size > 0
        }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_save))
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_item_contains_unsaved_changes), config = config) { selectedButton ->
            runOnUiThread {
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    saveItemAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                    closeDialog()
                }
            }
        }
    }

    protected fun closeDialog() {
        finish()
    }


    protected fun editItem(item: Item, source: Source?, series: Series? = source?.series, tags: MutableCollection<Tag>, files: MutableCollection<FileLink>,
                         updateContentPreview: Boolean = true) {
        itemToSave = item
        originalContent = item.content
        originalTags = tags

        contentToEdit = item.content

        if(item.summary.isEmpty() == false) { this.forceShowSummaryPreview = true } // forcing that once it has been shown it doesn't get hidden anymore

        source?.let { this.forceShowSourcePreview = true } // forcing that once it has been shown it doesn't get hidden anymore
        lytSourcePreview.setOriginalSourceToEdit(source, series, item.indication, this) { sourceChanged(it) }

        this.forceShowSourcePreview = forceShowSourcePreview || item.indication.isNotEmpty()

        tags.forEach { tag ->
            if(tagsOnItem.contains(tag) == false) { // to avoid have a tag twice we really have to check each single tag
                tagsOnItem.add(tag)
            }
        }

        forceShowTagsPreview = forceShowTagsPreview || tags.isNotEmpty()

        lytFilesPreview.setFiles(files, permissionsManager)
        forceShowFilesPreview = forceShowFilesPreview || files.isNotEmpty()

        updateDisplayedValuesOnUIThread(source, item.summary, updateContentPreview)
    }

    private fun updateDisplayedValuesOnUIThread(source: Source?, summaryToEdit: String, updateContentPreview: Boolean = true) {
        if(updateContentPreview) {
            setContentPreviewOnUIThread(source)
        }

        setTagsOnItemPreviewOnUIThread()

        setSourcePreviewOnUIThread()

        setSummaryPreviewOnUIThread(summaryToEdit)

        setFilesPreviewOnUIThread()
    }

    private fun restoreTagsOnItemAsync(tagsOnItemIdsString: String) {
        threadPool.runAsync { restoreTagsOnItem(tagsOnItemIdsString) }
    }

    private fun restoreTagsOnItem(tagsOnItemIdsString: String) {
        val restoredTagsOnItem = serializer.deserializeObject(tagsOnItemIdsString, List::class.java, Tag::class.java) as List<Tag>

        tagsOnItem.clear()
        tagsOnItem.addAll(restoredTagsOnItem)

        runOnUiThread { setTagsOnItemPreviewOnUIThread() }
    }


    private fun userClickedOnUrl(url: String) {
        openUrlOptionsView.showMenuCenter(txtItemContentLabel) { selectedOption ->
            when(selectedOption) {
                OpenUrlOptionsView.OpenUrlOption.OpenInNewActivity -> showUrlInNewActivity(url)
                OpenUrlOptionsView.OpenUrlOption.OpenWithOtherApp -> openUrlWithOtherApp(url)
            }
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

}
