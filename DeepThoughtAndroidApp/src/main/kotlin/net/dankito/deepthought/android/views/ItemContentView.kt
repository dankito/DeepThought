package net.dankito.deepthought.android.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.v4.app.ActivityCompat.invalidateOptionsMenu
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.webkit.WebView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_item_content.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.WebPageLoader
import net.dankito.utils.android.extensions.hideKeyboard
import net.dankito.utils.android.extensions.hideKeyboardDelayed
import net.dankito.deepthought.android.ui.UiStatePersister
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.richtexteditor.JavaScriptExecutorBase
import net.dankito.richtexteditor.android.FullscreenWebView
import net.dankito.richtexteditor.android.RichTextEditor
import net.dankito.utils.android.animation.ShowHideViewAnimator
import net.dankito.utils.android.OnSwipeTouchListener
import net.dankito.utils.android.ui.view.ToolbarUtil
import net.dankito.utils.IThreadPool
import net.dankito.utils.android.extensions.HtmlExtensions
import net.dankito.utils.android.permissions.IPermissionsService
import net.dankito.utils.ui.dialogs.IDialogService
import net.dankito.utils.ui.dialogs.ConfirmationDialogConfig
import net.dankito.utils.web.UrlUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject


class ItemContentView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val EDIT_CONTENT_HTML_INTENT_EXTRA_NAME = "EDIT_CONTENT_HTML"

        private const val IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME = "IS_IN_EDIT_CONTENT_MODE"
        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"

        private const val ShowHideEditContentViewAnimationDurationMillis = 500L

        private val ClickableElementTypes = listOf(WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE, WebView.HitTestResult.ANCHOR_TYPE)


        private val log = LoggerFactory.getLogger(ItemContentView::class.java)
    }


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var extractArticleHandler: ExtractArticleHandler

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var uiStatePersister: UiStatePersister

    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var threadPool: IThreadPool


    fun getCurrentHtmlAsync(callback: (html: String) -> Unit) {
        editHtmlView.getCurrentHtmlAsync(callback)
    }

    val isContentSet: Boolean
        get() {
            return contentToEdit.isBlank() == false && contentToEdit != "<p>\u200B</p>" // = RichTextEditor's default html
        }

    var didContentChange: Boolean = false
        protected set

    val isInEditContentMode: Boolean
        get() = ! contentEditor.isInViewingMode

    val shouldHideFloatingActionButton: Boolean
        get() = contentEditor.isInFullscreenMode || isInEditContentMode || lytContextHelpReaderView.visibility == View.VISIBLE

    val didUserEnterSomeContent: Boolean
        get() = dataManager.localSettings.didShowAddItemPropertiesHelp || isContentSet


    var didContentChangeListener: ((Boolean) -> Unit)? = null

    var fullscreenGestureListener: ((OnSwipeTouchListener.SwipeDirection) -> Unit)? = null


    private var originalContent: String? = null

    private lateinit var contentToEdit: String

    private lateinit var editItemView: IEditItemView


    private var isInReaderMode = false

    private var readerModeContent: String = ""

    private var webSiteHtml: String? = null

    private var isLoadingUrl = false


    private var lastShowOnboardingForItemProperties: Boolean = false

    private var lastShowContentOnboarding: Boolean? = null


    private lateinit var editHtmlView: EditHtmlView

    private var mnToggleReaderMode: MenuItem? = null

    protected val contextHelpUtil = ContextHelpUtil()

    private val animator = ShowHideViewAnimator()

    private val openUrlOptionsView = OpenUrlOptionsView()

    private var currentWebPageLoader: WebPageLoader? = null

    private val getAnchorUrlHandler = Handler()



    init {
        AppComponent.component.inject(this)

        setupUi()
    }

    private fun setupUi() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_item_content, this)

        setupItemContentEditor()

    }

    private fun setupItemContentEditor() {
        editHtmlView = EditHtmlView(context)
        editHtmlView.setupHtmlEditor(lytContentViewAndOnboardingText)

        editHtmlView.setHtmlChangedCallback { didChange ->
            this.didContentChange = didChange

            didContentChangeListener?.invoke(didChange)
        }

        btnApplyEditedContent.setOnClickListener { appliedChangesToContent() }

        contentEditor.enterViewingMode() // by default we start in viewing not editing mode

        contentEditor.elementClickedListener = { type -> elementInEditorClicked(type) }
    }


    private fun siteFinishedLoading(url: String, html: String) {
        urlLoadedNow()

        showContentInWebView(html, url)

        // now try to extract item content from WebView's html
        val extractionResult = editItemView.getItemExtractionResult()
        if(extractionResult != null && isInReaderMode == false) {
            webSiteHtml = html
            contentToEdit = html

            if(extractionResult.couldExtractContent == false) {
                extractContentAsync(extractionResult, html, url)
            }
        }
    }

    private fun extractContentAsync(extractionResult: ItemExtractionResult, html: String, url: String) {
        threadPool.runAsync {
            articleExtractorManager.extractArticleUserDidSeeBefore(extractionResult, html, url)

            if(extractionResult.couldExtractContent) {
                runOnUiThread {
                    extractedContentOnUiThread(extractionResult)
                }
            }
        }
    }

    private fun extractedContentOnUiThread(extractionResult: ItemExtractionResult) {
        readerModeContent = extractionResult.item.content

        mnToggleReaderMode?.isVisible = extractionResult.couldExtractContent
        invalidateOptionsMenu(context as Activity)

        editItemView.extractedContentOnUiThread(extractionResult)

        mayShowReaderViewHelp()
    }

    private fun mayShowReaderViewHelp() {
        val localSettings = dataManager.localSettings
        if(localSettings.didShowReaderViewHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpReaderView, R.string.context_help_reader_view) {
                editItemView.setFloatingActionButtonVisibilityOnUIThread()
                localSettings.didShowReaderViewHelp = true
                dataManager.localSettingsUpdated()
            }

            editItemView.setFloatingActionButtonVisibilityOnUIThread()
        }
    }

    private fun urlLoadedNow() {
        isLoadingUrl = false

        runOnUiThread {
            prgIsLoadingWebPage.visibility = View.GONE
        }
    }


    fun initialize(contentToEdit: String, editItemView: IEditItemView, permissionsService: IPermissionsService) {
        originalContent = contentToEdit
        this.contentToEdit = contentToEdit

        this.editItemView = editItemView

        isInReaderMode = editItemView.getItemExtractionResult()?.couldExtractContent ?: false
        readerModeContent = editItemView.getItemExtractionResult()?.item?.content ?: contentToEdit // TODO: is this correct?

        contentEditor.permissionsService = permissionsService

        contentEditor.changeFullscreenModeListener = { mode -> handleChangeFullscreenModeEvent(mode) }

        contentEditor.changeDisplayModeListener = { mode -> handleChangeDisplayModeEvent(mode) }

        contentEditor.swipeListener = { isInFullscreen, swipeDirection -> handleWebViewSwipe(isInFullscreen, swipeDirection) }
    }

    fun optionMenuCreated(mnToggleReaderMode: MenuItem, toolbarUtil: ToolbarUtil) {
        this.mnToggleReaderMode = mnToggleReaderMode

        mnToggleReaderMode?.isVisible = editItemView.getItemExtractionResult()?.couldExtractContent == true // show mnToggleReaderMode only if original web site has been shown before

        setReaderModeActionStateOnUIThread(toolbarUtil)
    }

    private fun setReaderModeActionStateOnUIThread(toolbarUtil: ToolbarUtil) {
        mnToggleReaderMode?.let { mnToggleReaderMode ->
            if(mnToggleReaderMode.isVisible == true) {
                if(isInReaderMode) {
                    mnToggleReaderMode.title = context.getString(R.string.action_website_view)
                    mnToggleReaderMode.setIcon(R.drawable.ic_reader_mode_disabled)
                }
                else {
                    mnToggleReaderMode.title = context.getString(R.string.action_reader_view)
                    mnToggleReaderMode.setIcon(R.drawable.ic_reader_mode)
                }

                toolbarUtil.updateMenuItemView(mnToggleReaderMode)
            }
        }
    }


    fun onResume(source: Source?) {
        // avoiding call to setContentPreviewOnUIThread() for now as on start it simply loads content a second time // TODO: find cases where it has been senseful
        // was needed for: Fixed that on resume non reader mode (= load url) hasn't been restored correctly or onboarding text got displayed
//        setContentPreviewOnUIThread(source) // TODO: is this really senseful in all circumstances or is it causing more trouble then solving problems?

        contentEditor.activityResumed()
    }

    fun onPause() {
        contentEditor.activityPaused()

        contentEditor.hideKeyboard()

        openUrlOptionsView.cleanUp()

        currentWebPageLoader?.cleanUp()
        currentWebPageLoader = null
    }

    fun onDestroy() {
        pauseWebView()
    }

    fun handlesBackButtonPress(didOtherDataChange: Boolean): Boolean {
        if(openUrlOptionsView.handlesBackButtonPress()) {
            return true
        }
        else if(isInEditContentMode) {
            if(editHtmlView.handlesBackButtonPress()) {
                return true
            }

            if(didOtherDataChange || didContentChange) { // if creating a new item and no value (either tags, source, ...) has been set, leave EditItemActivity directly, don't just leave editing mode (as there's nothing to see)
                appliedChangesToContent() // otherwise apply changes
                return true
            }
        }

        return false
    }

    fun handleOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnToggleReaderMode -> {
                toggleReaderMode()
                return true
            }
        }

        return false
    }


    private fun setContentPreviewOnUIThread() {
        setContentPreviewOnUIThread(editItemView.currentSource)
    }

    fun setContentPreviewOnUIThread(source: Source?) {
        contentEditor.javaScriptExecutor.addLoadedListener {
            runOnUiThread {
                setContentPreviewAfterLoadingEditorOnUIThread(source)
            }
        }
    }

    fun setContentPreviewAfterLoadingEditorOnUIThread(source: Source?) {
        val content = contentToEdit
        val url = source?.url
        var showContentOnboarding = isContentSet == false
        prgIsLoadingWebPage.visibility = View.GONE

        if(shouldShowContent(content)) {
            showContentInWebView(content, url)
            showContentOnboarding = false
        }
        else if(isInReaderMode == false && webSiteHtml != null) {
            showContentInWebView(webSiteHtml, url)
            showContentOnboarding = false
        }
        else if(url != null && editItemView.getItemExtractionResult() != null) { // then load url (but don't show it for an Item)
            isLoadingUrl = true
            prgIsLoadingWebPage.visibility = View.VISIBLE

            currentWebPageLoader?.cleanUp()
            currentWebPageLoader = WebPageLoader(context as Activity)
            // in retrievedBaseHtmlCallback set url to null so that RichTextEditor doesn't load referenced parts (scripts, ...) that WebPageLoader also does in background
            currentWebPageLoader?.loadUrl(url, { showContentInWebView(it, null) } ) { html -> siteFinishedLoading(url, html) }
            showContentOnboarding = false
        }

        setOnboardingTextVisibilityOnUIThread(lastShowOnboardingForItemProperties, showContentOnboarding)
    }

    private fun shouldShowContent(content: String?): Boolean {
        // TODO: currently we assume that for item content is always set, this may change in the feature
        val extractionResult = editItemView.getItemExtractionResult()

        return isContentSet &&
                (extractionResult == null || (isInReaderMode && extractionResult?.couldExtractContent == true) ) // extractionResult == null -> it's an Item
    }

    private fun showContentInWebView(content: String?, url: String?) {
        editHtmlView.setHtml(content ?: "", url)
    }


    private fun mayShowOnboardingTextVisibilityOnUIThread() {
        mayShowOnboardingTextVisibilityOnUIThread(lastShowOnboardingForItemProperties, lastShowContentOnboarding)
    }

    fun mayShowOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties: Boolean, showContentOnboarding: Boolean? = null) {
        this.lastShowOnboardingForItemProperties = showOnboardingForItemProperties
        this.lastShowContentOnboarding = showContentOnboarding

        val calculatedShowContentOnboarding = if(showContentOnboarding != null) showContentOnboarding else isContentSet == false

        setOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties, calculatedShowContentOnboarding)
    }

    private fun setOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties: Boolean, showContentOnboarding: Boolean? = null) {
        if(isInEditContentMode == false && (showContentOnboarding == true || showOnboardingForItemProperties)) {
            lytOnboardingText.visibility = View.VISIBLE
            lytContentViewAndOnboardingText.setOnClickListener { editContent() } // only enable editing content by clicking on lytContentViewAndOnboardingText when showing onboarding text

            val onboardingTextId = if(showContentOnboarding == true) R.string.activity_edit_item_edit_content_onboarding_text else R.string.activity_edit_item_add_item_properties_onboarding_text
            txtOnboardingText.text = HtmlExtensions.getSpannedFromHtml(context, onboardingTextId)

            arrowToFloatingActionButton.visibility = if(showContentOnboarding != true && showOnboardingForItemProperties) View.VISIBLE else View.GONE
        }
        else {
            lytOnboardingText.visibility = View.GONE
            lytContentViewAndOnboardingText.setOnClickListener(null)
        }

        if(showContentOnboarding == true && isInEditContentMode == false) {
            lytContentView.visibility = View.GONE
        }
        else if(showContentOnboarding == false) {
            lytContentView.visibility = View.VISIBLE
        }
    }




    private fun handleChangeFullscreenModeEvent(mode: FullscreenWebView.FullscreenMode) {
        when(mode) {
            FullscreenWebView.FullscreenMode.Enter -> enterFullscreenMode()
            FullscreenWebView.FullscreenMode.Leave -> leaveFullscreenMode()
        }
    }

    private fun handleChangeDisplayModeEvent(mode: FullscreenWebView.DisplayMode) {
        when(mode) {
            FullscreenWebView.DisplayMode.Viewing -> leftEditContentView()
            FullscreenWebView.DisplayMode.Editing -> leftViewContentView()
        }
    }

    private fun leftEditContentView() {
        contentEditor.hideKeyboardDelayed(250) // for Samsungs we need a delay (again an exception of Samsung devices, i really dislike them)

        animator.playShowAnimation(editItemView.itemFieldsPreview)

        txtItemContentLabel.visibility = View.VISIBLE
        txtEnterContentHint.visibility = View.GONE

        editItemView.viewToolbar.visibility = View.VISIBLE
        editItemView.setFloatingActionButtonVisibilityOnUIThread()
        mayShowOnboardingTextVisibilityOnUIThread()

        invalidateOptionsMenu(context as Activity)
    }

    private fun leftViewContentView() {
        val webViewContentLocation = IntArray(2)
        contentEditor.getLocationInWindow(webViewContentLocation)
        val start = webViewContentLocation[1].toFloat()
        playShowEditContentViewAnimation(start)

        txtItemContentLabel.visibility = View.GONE
        txtEnterContentHint.visibility = if(didUserEnterSomeContent) View.GONE else View.VISIBLE

        editItemView.viewToolbar.visibility = View.GONE
        editItemView.setFloatingActionButtonVisibilityOnUIThread()
        lytOnboardingText.visibility = View.GONE

        invalidateOptionsMenu(context as Activity)
    }

    private fun playShowEditContentViewAnimation(start: Float) {
        val itemFieldsPreview = editItemView.itemFieldsPreview
        val interpolator = AccelerateInterpolator()

        val fieldsPreviewYAnimator = ObjectAnimator
                .ofFloat(itemFieldsPreview, View.Y, itemFieldsPreview.top.toFloat(), -1f * itemFieldsPreview.measuredHeight)
                .setDuration(ShowHideEditContentViewAnimationDurationMillis)
        fieldsPreviewYAnimator.interpolator = interpolator

        val editContentViewYAnimator = ObjectAnimator
                .ofFloat(contentEditor, View.Y, start, 0f)
                .setDuration(ShowHideEditContentViewAnimationDurationMillis / 2)
        editContentViewYAnimator.interpolator = interpolator

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fieldsPreviewYAnimator, editContentViewYAnimator)

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) { }

            override fun onAnimationRepeat(animation: Animator?) { }

            override fun onAnimationCancel(animation: Animator?) { }

            override fun onAnimationEnd(animation: Animator?) {
                itemFieldsPreview.visibility = View.GONE // hide itemFieldsPreview so that editor uses all available space
            }

        })
        animatorSet.start()
    }

    private fun handleWebViewSwipe(isInFullscreen: Boolean, swipeDirection: OnSwipeTouchListener.SwipeDirection) {
        if(isInFullscreen && isInEditContentMode == false && fullscreenGestureListener != null) {
            when(swipeDirection) {
                OnSwipeTouchListener.SwipeDirection.Left,
                OnSwipeTouchListener.SwipeDirection.Right -> {
                    mayShowItemInformationFullscreenGesturesHelpOnUIThread { fullscreenGestureListener?.invoke(swipeDirection) }
                }
            }
        }
    }

    private fun mayShowItemInformationFullscreenGesturesHelpOnUIThread(userConfirmedHelpOnUIThread: () -> Unit) {
        val localSettings = dataManager.localSettings

        if(localSettings.didShowItemInformationFullscreenGesturesHelp == false) {
            dialogService.showConfirmationDialog(context.getString(R.string.context_help_item_content_fullscreen_gestures), config = ConfirmationDialogConfig(false)) {
                runOnUiThread {
                    contentEditor.leaveFullscreenModeAndWaitTillLeft { // leave fullscreen otherwise a lot of unwanted behaviour occurs
                        userConfirmedHelpOnUIThread()
                    }
                }
            }

            localSettings.didShowItemInformationFullscreenGesturesHelp = true
            dataManager.localSettingsUpdated()
        }
        else {
            contentEditor.leaveFullscreenModeAndWaitTillLeft {// leave fullscreen otherwise a lot of unwanted behaviour occurs
                userConfirmedHelpOnUIThread()
            }
        }
    }


    private fun leaveFullscreenMode() {
        txtItemContentLabel.visibility = View.VISIBLE

        editItemView.itemFieldsPreview.visibility = View.VISIBLE
        editItemView.viewToolbar.visibility = View.VISIBLE
        mayShowOnboardingTextVisibilityOnUIThread()
        editItemView.setFloatingActionButtonVisibilityOnUIThread()
    }

    private fun enterFullscreenMode() {
        txtItemContentLabel.visibility = View.GONE
        lytOnboardingText.visibility = View.GONE

        editItemView.itemFieldsPreview.visibility = View.GONE
        editItemView.viewToolbar.visibility = View.GONE
        editItemView.setFloatingActionButtonVisibilityOnUIThread()

        (parent?.parent as? ViewGroup)?.invalidate()

        mayShowItemInformationFullscreenHelpOnUIThread()
    }

    private fun mayShowItemInformationFullscreenHelpOnUIThread() {
        val localSettings = dataManager.localSettings

        if(localSettings.didShowItemInformationFullscreenHelp == false) {
            contextHelpUtil.showContextHelp(lytContextHelpFullscreenMode, R.string.context_help_item_content_fullscreen)

            localSettings.didShowItemInformationFullscreenHelp = true
            dataManager.localSettingsUpdated()
        }
    }


    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()

        val outState = Bundle()


        outState.putBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, isInEditContentMode)
        outState.putBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, isInReaderMode)

        if(contentToEdit != originalContent) {
            uiStatePersister.serializeStateToDiskIfNotNull(outState, CONTENT_INTENT_EXTRA_NAME, contentToEdit) // application crashes if objects put into bundle are too large (> 1 MB) for Android
        }

        if(isInEditContentMode) {
            uiStatePersister.serializeStateToDiskIfNotNull(outState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, editHtmlView.getCurrentHtmlBlocking()) // application crashes if objects put into bundle are too large (> 1 MB) for Android
        }

        return outState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(AbsSavedState.EMPTY_STATE) // don't call with state as super.onRestoreInstanceState() doesn't like a Bundle as parameter value

        (state as? Bundle)?.let { savedInstanceState ->
            val isInEditContentMode = savedInstanceState.getBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, false)
            this.isInReaderMode = savedInstanceState.getBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, false)

            uiStatePersister.restoreStateFromDisk(savedInstanceState, CONTENT_INTENT_EXTRA_NAME, String::class.java)?.let { content ->
                contentToEdit = content
                setContentPreviewOnUIThread()
            }

            uiStatePersister.restoreStateFromDisk(savedInstanceState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, String::class.java)?.let {
                showContentInWebView(it, editItemView.currentSource?.url)
            }

            if(isInEditContentMode) {
                editContent()
            }
        }
    }


    fun editContent() {
        if(isLoadingUrl) { // while WebView is still loading contentToEdit is not set yet (contentToEdit gets set when loading finishes)
            return
        }

        lytContentView.visibility = View.VISIBLE

        contentEditor.enterEditingMode()
    }

    private fun appliedChangesToContent() {
        contentEditor.getCurrentHtmlAsync { html -> // update contentToEdit as paste or Samsung's Swipe keyboard doesn't raise changed event -> fetch value before saving
            contentToEdit = html

            runOnUiThread {
                leaveEditContentView()

                didContentChangeListener?.invoke(originalContent != contentToEdit) // TODO: is this really needed as actually calling didContentChangeListener in setupItemContentEditor() should be enough
            }
        }
    }




    private fun leaveEditContentView() {
        contentEditor.enterViewingMode()
    }

    private fun pauseWebView() {
        // to prevent that a video keeps on playing in WebView when navigating away from EditItemActivityBase
        // see https://stackoverflow.com/a/6230902
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause")
                    .invoke(contentEditor)

            Class.forName("android.webkit.WebView")
                    .getMethod("destroy") // so that also pod casts are for sure stopped
                    .invoke(contentEditor)

        } catch(ignored: Exception) { }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(openUrlOptionsView.handlesTouch(event)) {
            return true
        }

        return super.dispatchTouchEvent(event)
    }


    private fun toggleReaderMode() {
        isInReaderMode = !isInReaderMode

        if(isInReaderMode) {
            contentToEdit = readerModeContent
        }
        else {
            readerModeContent = contentToEdit
            contentToEdit = webSiteHtml ?: ""
        }

        setContentPreviewOnUIThread()
        invalidateOptionsMenu(context as Activity)

        checkIsShowingReaderViewHelp()
    }

    private fun checkIsShowingReaderViewHelp() {
        if (dataManager.localSettings.didShowReaderViewHelp == false) {
            dataManager.localSettings.didShowReaderViewHelp = true
            dataManager.localSettingsUpdated()

            if(lytContextHelpReaderView.visibility == View.VISIBLE) {
                contextHelpUtil.animateHideContextHelp(lytContextHelpReaderView) {
                    editItemView.setFloatingActionButtonVisibilityOnUIThread()
                }
            }
        }
    }


    /**
     * Don't know why webViewClient doesn't receive clicks on urls anymore, so handling them manually
     */
    private fun elementInEditorClicked(type: Int): Boolean {
        if(contentEditor.isInViewingMode && ClickableElementTypes.contains(type)) {
            getClickedUrl(contentEditor, type)?.let { clickedUrl ->
                if(urlUtil.isHttpUri(clickedUrl)) {
                    userClickedOnUrl(clickedUrl)
                    // do not return true even though we handled click as otherwise text user clicked on would get selected (still have to figure out why?)
                }
            }
        }

        return false
    }

    private fun getClickedUrl(contentEditor: RichTextEditor, clickedElementType: Int): String? {
        // if user clicked on an image in an anchor, we want to get anchor's src url not image's src, but contentEditor.hitTestResult?.extra contains image's src
        // -> get anchor's src, see e. g. https://stackoverflow.com/questions/12168039/how-to-get-link-url-in-android-webview-with-hittestresult-for-a-linked-image-an
        if (clickedElementType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            val message = getAnchorUrlHandler.obtainMessage()
            contentEditor.requestFocusNodeHref(message)

            val url = message?.data?.get("url") as? String
            val imageSrc = message?.data?.get("src") as? String

            if (url == imageSrc) { // if <img> is in an <a> element, then url != imageSrc
                return null // if <img> is not in an <a> element, don't offer to open image in other application
            }

            return url
        }

        return contentEditor.hitTestResult?.extra // extra contains url if clicked on a link
    }

    private fun userClickedOnUrl(url: String) {
        if(url.startsWith(JavaScriptExecutorBase.EditorStateChangedCallbackScheme)) {
            return
        }

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
            context.startActivity(intent)
        } catch(e: Exception) { log.error("Could not open url $url with other app", e) }
    }


    private fun runOnUiThread(action: () -> Unit) {
        (context as? Activity)?.runOnUiThread(action)
    }

}