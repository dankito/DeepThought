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
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.ItemChanged
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


open class EditItemActivityBase : BaseActivity() {

    companion object {
        private const val ITEM_ID_INTENT_EXTRA_NAME = "ITEM_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"

        private const val CHANGED_FIELDS_INTENT_EXTRA_NAME = "CHANGED_FIELDS"

        private const val FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_TAGS_PREVIEW"
        private const val FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SOURCE_PREVIEW"
        private const val FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SUMMARY_PREVIEW"
        private const val FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_FILES_PREVIEW"

        private const val IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME = "IS_IN_EDIT_CONTENT_MODE"
        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val EDIT_CONTENT_HTML_INTENT_EXTRA_NAME = "EDIT_CONTENT_HTML"
        private const val TAGS_ON_ITEM_INTENT_EXTRA_NAME = "TAGS_ON_ITEM"
        private const val FILES_INTENT_EXTRA_NAME = "ATTACHED_FILES"

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


    private var item: Item? = null

    private var readLaterArticle: ReadLaterArticle? = null


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


    private val contextHelpUtil = ContextHelpUtil()

    private val toolbarUtil = ToolbarUtil()

    private val openUrlOptionsView = OpenUrlOptionsView()

    private val permissionsManager: IPermissionsService

    private lateinit var editHtmlView: EditHtmlView

    private val animator = ShowHideViewAnimator()

    protected var mnSaveItem: MenuItem? = null

    protected var mnDeleteExistingItem: MenuItem? = null

    protected var mnToggleReaderMode: MenuItem? = null

    protected var mnSaveItemExtractionResultForLaterReading: MenuItem? = null

    protected var mnDeleteReadLaterArticle: MenuItem? = null

    protected var mnShareItem: MenuItem? = null

    private lateinit var floatingActionMenu: EditItemActivityFloatingActionMenuButton


    private val dataManager: DataManager

    private var eventBusListener: EventBusListener? = null


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

        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ITEM_ID_INTENT_EXTRA_NAME)?.let { itemId -> editItem(itemId) }

        if(getItemExtractionResult() == null && savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME) == null &&
                savedInstanceState.getString(ITEM_ID_INTENT_EXTRA_NAME) == null) { // a new Item is being created then
            createItem(false) // don't go to EditHtmlTextDialog for content here as we're restoring state, content may already be set
        }

        restoreStateFromDisk(savedInstanceState, CONTENT_INTENT_EXTRA_NAME, String::class.java)?.let { content ->
            contentToEdit = content
            setContentPreviewOnUIThread()
        }

        savedInstanceState.getString(TAGS_ON_ITEM_INTENT_EXTRA_NAME)?.let { tagsOnItemIds -> restoreTagsOnItemAsync(tagsOnItemIds) }
        // TODO:
//        savedInstanceState.getString(FILES_INTENT_EXTRA_NAME)?.let { fileIds -> restoreFilesAsync(fileIds) }

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

    protected open fun restoreEntity(savedInstanceState: Bundle) {

    }

    protected open fun saveState(outState: Bundle) {

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(ITEM_ID_INTENT_EXTRA_NAME, null)
            item?.id?.let { itemId -> outState.putString(ITEM_ID_INTENT_EXTRA_NAME, itemId) }

            outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, null)
            readLaterArticle?.id?.let { readLaterArticleId -> outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticleId) }

            saveState(outState)

            outState.putIntArray(CHANGED_FIELDS_INTENT_EXTRA_NAME, changedFields.map { it.ordinal }.toIntArray())

            outState.putBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, forceShowTagsPreview)
            outState.putBoolean(FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME, forceShowSourcePreview)
            outState.putBoolean(FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME, forceShowSummaryPreview)
            outState.putBoolean(FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME, forceShowFilesPreview)

            outState.putBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, isInEditContentMode)
            outState.putBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, isInReaderMode)

            outState.putString(TAGS_ON_ITEM_INTENT_EXTRA_NAME, serializer.serializeObject(tagsOnItem))
            // TODO: add PersistedFilesSerializer
            outState.putString(FILES_INTENT_EXTRA_NAME, serializer.serializeObject(lytFilesPreview.getEditedFiles()))

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
        // if ItemExtractionResult's item content hasn't been extracted yet, wait till WebView is loaded and extract item content then
        if(getItemExtractionResult() != null && isInReaderMode == false &&
                webView.url != null && webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
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
        val extractionResult = getItemExtractionResult() ?: readLaterArticle?.itemExtractionResult
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
        return readLaterArticle?.itemExtractionResult
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

        mayRegisterEventBusListener()
        lytSourcePreview.viewBecomesVisible()
        lytTagsPreview.viewBecomesVisible()
        lytFilesPreview.viewBecomesVisible()
    }


    private fun editContent() {
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
        if(haveAllFieldsOfExistingItemBeenCleared()) {
            mnSaveItem?.isVisible = false
        }
        else {
            mnSaveItem?.isVisible = item == null // ItemExtractionResult and ReadLaterArticle always can be saved
                    || item?.isPersisted() == false || changedFields.size > 0
        }
    }

    private fun haveAllFieldsOfExistingItemBeenCleared(): Boolean {
        if(item != null && item?.isPersisted() == true) {
            return haveAllFieldsBeenCleared()
        }

        return false
    }

    private fun haveAllFieldsBeenCleared(): Boolean {
        return contentToEdit.isNullOrBlank() && tagsOnItem.isEmpty() && lytSourcePreview.source == null
                && lytSummaryPreview.getCurrentFieldValue().isEmpty() && lytFilesPreview.getEditedFiles().size == 0
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
        else if(url != null && item == null) { // then load url (but don't show it for an Item)
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
                (item != null || (isInReaderMode && getItemExtractionResult()?.couldExtractContent == true) )
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
        mnShareItem?.isVisible = lytSourcePreview.source?.url.isNullOrBlank() == false
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
        unregisterEventBusListener()
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
            if(item == null || item?.isPersisted() == true || this.haveAllFieldsBeenCleared() == false) {
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
        readLaterArticle?.let { mnSaveItem?.setIcon(R.drawable.ic_tab_items) }

        mnDeleteExistingItem = menu.findItem(R.id.mnDeleteExistingItem)
        mnDeleteExistingItem?.isVisible = item?.isPersisted() == true

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        mnToggleReaderMode?.isVisible = readLaterArticle?.itemExtractionResult?.couldExtractContent == true /*&& webSiteHtml != null*/ // show mnToggleReaderMode only if previously original web site was shown
        setReaderModeActionStateOnUIThread()

        mnSaveItemExtractionResultForLaterReading = menu.findItem(R.id.mnSaveItemExtractionResultForLaterReading)

        mnDeleteReadLaterArticle = menu.findItem(R.id.mnDeleteReadLaterArticle)
        mnDeleteReadLaterArticle?.isVisible = readLaterArticle != null

        mnShareItem = menu.findItem(R.id.mnShareItem)
        updateShowMenuItemShareItem()

        setMenuSaveItemVisibleStateOnUIThread()

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
            R.id.mnSaveItem -> {
                saveItemAndCloseDialog()
                return true
            }
            R.id.mnDeleteExistingItem -> {
                askIfShouldDeleteExistingItemAndCloseDialog()
                return true
            }
            R.id.mnToggleReaderMode -> {
                toggleReaderMode()
                return true
            }
            R.id.mnDeleteReadLaterArticle -> {
                deleteReadLaterArticleAndCloseDialog()
                return true
            }
            R.id.mnShareItem -> {
                showShareItemPopupMenu()
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
            contentToEdit = item?.content ?: getItemExtractionResult()?.item?.content ?: ""
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

    private fun showShareItemPopupMenu() {
        val overflowMenuButton = getOverflowMenuButton()
        if(overflowMenuButton == null) {
            return
        }

        val popup = PopupMenu(this, overflowMenuButton)

        popup.menuInflater.inflate(R.menu.share_item_menu, popup.menu)

        val source = lytSourcePreview.source
        if(source == null || source.url.isNullOrBlank()) {
            popup.menu.findItem(R.id.mnShareItemSourceUrl).isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.mnShareItemSourceUrl -> shareSourceUrl()
                R.id.mnShareItemContent -> shareItemContent()
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
        mnSaveItemExtractionResultForLaterReading?.isEnabled = false
        unregisterEventBusListener()

        saveItemAsync { successful ->
            if(successful) {
                mayShowSavedReadLaterArticleHelpAndCloseDialog()
            }
            else {
                mnSaveItem?.isEnabled = true
                mnSaveItemExtractionResultForLaterReading?.isEnabled = true
                mayRegisterEventBusListener()
            }
        }
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
            if(successful) {
                readLaterArticle?.let { readLaterArticle ->
                    readLaterArticleService.delete(readLaterArticle)
                }

                setActivityResult(EditItemActivityResult(didSaveItem = true, savedItem = itemToSave))
            }
            callback(successful)
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
        val localSettings = itemService.dataManager.localSettings

        if(readLaterArticle != null && localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp == false) {
            localSettings.didShowSavedReadLaterArticleIsNowInItemsHelp = true
            itemService.dataManager.localSettingsUpdated()

            val helpText = getText(R.string.context_help_saved_read_later_article_is_now_in_items).toString()
            dialogService.showConfirmationDialog(contextHelpUtil.stringUtil.getSpannedFromHtml(helpText), config = ConfirmationDialogConfig(false)) {
                callback()
            }
        }
        else {
            callback()
        }
    }


    private fun askIfShouldDeleteExistingItemAndCloseDialog() {
        item?.let { item ->
            dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_delete_item, item.preview)) { selectedButton ->
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    mnDeleteExistingItem?.isEnabled = false
                    unregisterEventBusListener()

                    deleteEntityService.deleteItem(item) // TODO: move to presenter
                    closeDialog()
                }
            }
        }
    }

    private fun deleteReadLaterArticleAndCloseDialog() {
        readLaterArticle?.let { readLaterArticle ->
            mnSaveItem?.isEnabled = false // disable to that save cannot be pressed a second time
            mnDeleteReadLaterArticle?.isEnabled = false
            unregisterEventBusListener()

            presenter.deleteReadLaterArticle(readLaterArticle)

            runOnUiThread { closeDialog() }
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
        readLaterArticle?.itemExtractionResult?.series = null
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


    protected open fun showParameters(parameters: EditItemActivityParameters) {
        parameters.item?.let { editItem(it) }

        parameters.readLaterArticle?.let {
            isInReaderMode = it.itemExtractionResult.couldExtractContent
            editReadLaterArticle(it)
        }

        if(parameters.createItem) {
            createItem()
        }
    }

    private fun createItem(editContent: Boolean = true) {
        editItem(Item(""))

        if(editContent) {
            editContent() // go directly to edit content dialog, there's absolutely nothing to see on this almost empty screen
        }
    }

    private fun editItem(itemId: String) {
        itemService.retrieve(itemId)?.let { item ->
            editItem(item)
        }
    }

    private fun editItem(item: Item) {
        this.item = item

        mnDeleteExistingItem?.isVisible = item.isPersisted()

        editItem(item, item.source, item.source?.series, item.tags, item.attachedFiles)
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            editReadLaterArticle(readLaterArticle)
        }
    }

    private fun editReadLaterArticle(readLaterArticle: ReadLaterArticle, updateContentPreview: Boolean = true) {
        this.readLaterArticle = readLaterArticle
        val extractionResult = readLaterArticle.itemExtractionResult

        mnSaveItem?.setIcon(R.drawable.ic_tab_items)
        editItem(extractionResult.item, extractionResult.source, extractionResult.series, extractionResult.tags, extractionResult.files, updateContentPreview)
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

    private fun updateDisplayedValuesOnUIThread(source: Source? = lytSourcePreview.source, summaryToEdit: String = lytSummaryPreview.getCurrentFieldValue(), updateContentPreview: Boolean = true) {
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


    protected fun mayRegisterEventBusListener() {
        if(item?.isPersisted() == true && eventBusListener == null) {
            synchronized(this) {
                val eventBusListenerInit = EventBusListener()

                eventBus.register(eventBusListenerInit)

                this.eventBusListener = eventBusListenerInit
            }
        }
    }

    protected fun unregisterEventBusListener() {
        synchronized(this) {
            eventBusListener?.let {
                eventBus.unregister(it)
            }

            this.eventBusListener = null
        }
    }

    private fun warnItemHasBeenEdited() {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_item_alert_message_item_has_been_edited))
        }
    }

    private fun updateDisplayedValues() {
        // TODO: this is wrong, we have to re-fetch content, summary, tags, source, series and files from item, itemExtractionResult or readLaterArticle
        runOnUiThread { updateDisplayedValuesOnUIThread() }
    }

    inner class EventBusListener {

        @Handler
        fun itemChanged(change: ItemChanged) { // TODO: what about ReadLaterArticle?
            if(change.entity.id == item?.id) {
                if(change.source == EntityChangeSource.Synchronization && change.isDependentChange == false) {
                    warnItemHasBeenEdited()
                }
                else { // TODO: or will changes then may get overwritten? As sometimes it's really senseful to update values, e.g. when a file synchronization state gets updated
                    updateDisplayedValues()
                }
            }
        }
    }

}