package net.dankito.deepthought.android.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.ActivityCompat.invalidateOptionsMenu
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.webkit.*
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_item_content.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.hideKeyboardDelayed
import net.dankito.deepthought.android.ui.UiStatePersister
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.richtexteditor.android.animation.ShowHideViewAnimator
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


class ItemContentView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val EDIT_CONTENT_HTML_INTENT_EXTRA_NAME = "EDIT_CONTENT_HTML"

        private const val IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME = "IS_IN_EDIT_CONTENT_MODE"
        private const val IS_IN_READER_MODE_INTENT_EXTRA_NAME = "IS_IN_READER_MODE"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"

        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"

        private const val ShowHideEditContentViewAnimationDurationMillis = 500L


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


    val currentValue: String
        get() = contentToEdit

    val shouldHideFloatingActionButton: Boolean
        get() = wbvwContent.isInFullscreenMode || isEditingContent() || lytContextHelpReaderView.visibility == View.VISIBLE

    var didContentChangeListener: ((Boolean) -> Unit)? = null

    var fullscreenGestureListener: ((OnSwipeTouchListener.SwipeDirection) -> Unit)? = null


    private var originalContent: String? = null

    private lateinit var contentToEdit: String

    private lateinit var editItemView: IEditItemView


    var isInEditContentMode = false
        private set

    private var isInReaderMode = false

    private var readerModeContent: String = ""

    private var webSiteHtml: String? = null

    private var isLoadingUrl = false


    private var lastShowOnboardingForItemProperties: Boolean = false

    private var lastShowContentOnboarding: Boolean? = null


    private lateinit var editHtmlView: EditHtmlView

    private lateinit var mnToggleReaderMode: MenuItem

    protected val contextHelpUtil = ContextHelpUtil()

    private val animator = ShowHideViewAnimator()

    private val openUrlOptionsView = OpenUrlOptionsView()



    init {
        AppComponent.component.inject(this)

        setupUi()
    }

    private fun setupUi() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_item_content, this)

        wbvwContent?.requestFocus() // avoid that lytSummaryPreview gets focus and keyboard therefore gets displayed on activity start

        setupItemContentView()

        setupItemContentEditor()

    }

    private fun setupItemContentEditor() {
        editHtmlView = EditHtmlView(context)
        editHtmlView.setupHtmlEditor(lytEditContent)

        editHtmlView.setHtmlChangedCallback { didChange ->
            didContentChangeListener?.invoke(didChange)
        }
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


    inner class GetHtmlCodeFromWebViewJavaScripInterface(private val retrievedHtmlCode: ((url: String, html: String) -> Unit)) {

        @JavascriptInterface
        @SuppressWarnings("unused")
        fun finishedLoadingSite(url: String, html: String) {
            retrievedHtmlCode(url, html)
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
        val extractionResult = editItemView.getItemExtractionResult()

        if(extractionResult == null) { // an item
            urlLoadedNow()
        }
        // if ItemExtractionResult's item content hasn't been extracted yet, wait till WebView is loaded and extract item content then
        else if(extractionResult != null && isInReaderMode == false &&
                webView.url != null && webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
            webView.loadUrl("javascript:$GetHtmlCodeFromWebViewJavaScriptInterfaceName.finishedLoadingSite" +
                    "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }
    }

    private fun siteFinishedLoading(url: String, html: String) {
        urlLoadedNow()

        // now try to extract item content from WebView's html
        val extractionResult = editItemView.getItemExtractionResult()
        if(extractionResult != null && isInReaderMode == false) {
            webSiteHtml = html
            contentToEdit = html

            if(extractionResult.couldExtractContent == false) {
                articleExtractorManager.extractArticleUserDidSeeBefore(extractionResult, html, url)

                if(extractionResult.couldExtractContent) {
                    runOnUiThread {
                        extractedContentOnUiThread(extractionResult)
                    }
                }
            }
        }
    }

    private fun extractedContentOnUiThread(extractionResult: ItemExtractionResult) {
        wbvwContent.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        readerModeContent = extractionResult.item.content

        mnToggleReaderMode.isVisible = extractionResult.couldExtractContent
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
        wbvwContent.elementClickedListener = null

        runOnUiThread {
            wbvwContent.setWebViewClient(webViewClient) // now reactivate default url handling
            prgIsLoadingWebPage.visibility = View.GONE
        }
    }


    fun initialize(contentToEdit: String, editItemView: IEditItemView) {
        originalContent = contentToEdit
        this.contentToEdit = contentToEdit

        this.editItemView = editItemView

        isInReaderMode = editItemView.getItemExtractionResult()?.couldExtractContent ?: false
        readerModeContent = editItemView.getItemExtractionResult()?.item?.content ?: contentToEdit // TODO: is this correct?
    }

    fun optionMenuCreated(mnToggleReaderMode: MenuItem, toolbarUtil: ToolbarUtil) {
        this.mnToggleReaderMode = mnToggleReaderMode

        mnToggleReaderMode.isVisible = editItemView.getItemExtractionResult()?.couldExtractContent == true // show mnToggleReaderMode only if original web site has been shown before

        setReaderModeActionStateOnUIThread(toolbarUtil)
    }

    private fun setReaderModeActionStateOnUIThread(toolbarUtil: ToolbarUtil) {
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


    fun onResume(source: Source?) {
        setContentPreviewOnUIThread(source) // TODO: is this really senseful in all circumstances or is it causing more trouble then solving problems?

        wbvwContent.activityResumed()
    }

    fun onPause() {
        wbvwContent.activityPaused()

        contentEditor.hideKeyboard()
    }

    fun onDestroy() {
        pauseWebView()

    }

    fun handlesBackButtonPress(isCreatingNewItemAndAllFieldsHaveBeenCleared: Boolean): Boolean {
        if(openUrlOptionsView.handlesBackButtonPress()) {
            return true
        }
        else if(isInEditContentMode) {
            if(isCreatingNewItemAndAllFieldsHaveBeenCleared) { // if creating an item and no value has been set, leave EditItemActivity directly, don't just hide contentEditor (as there's nothing to see)
                leaveEditContentView()
                return true
            }
        }

        return false
    }

    fun handleOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                if(isInEditContentMode) {
                    leaveEditContentView()
                    return true
                }
            }
            R.id.mnToggleReaderMode -> {
                toggleReaderMode()
                return true
            }
            R.id.mnApplyHtmlChanges -> {
                appliedChangesToContent()
                return true
            }
        }

        return false
    }


    private fun setContentPreviewOnUIThread() {
        setContentPreviewOnUIThread(editItemView.currentSource)
    }

    fun setContentPreviewOnUIThread(source: Source?) {
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
        else if(url != null && editItemView.getItemExtractionResult() != null) { // then load url (but don't show it for an Item)
            clearWebViewItem()
            isLoadingUrl = true
            wbvwContent.elementClickedListener = { true } // disable link clicks during loading url
            wbvwContent.setWebViewClient(WebViewClient()) // to avoid that redirects open url in browser
            prgIsLoadingWebPage.visibility = View.VISIBLE
            wbvwContent.loadUrl(url)
            showContentOnboarding = false
        }

        setOnboardingTextVisibilityOnUIThread(showContentOnboarding)
    }

    private fun shouldShowContent(content: String?): Boolean {
        // TODO: currently we assume that for item content is always set, this may change in the feature
        val extractionResult = editItemView.getItemExtractionResult()

        return content.isNullOrBlank() == false &&
                (extractionResult == null || (isInReaderMode && extractionResult?.couldExtractContent == true) ) // extractionResult == null -> it's an Item
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


    private fun mayShowOnboardingTextVisibilityOnUIThread() {
        mayShowOnboardingTextVisibilityOnUIThread(lastShowOnboardingForItemProperties, lastShowContentOnboarding)
    }

    fun mayShowOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties: Boolean, showContentOnboarding: Boolean? = null) {
        this.lastShowOnboardingForItemProperties = showOnboardingForItemProperties
        this.lastShowContentOnboarding = showContentOnboarding

        val calculatedShowContentOnboarding = if(showContentOnboarding == null) lytContentWebView.visibility != View.VISIBLE else showContentOnboarding

        setOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties, calculatedShowContentOnboarding)
    }

    private fun setOnboardingTextVisibilityOnUIThread(showOnboardingForItemProperties: Boolean, showContentOnboarding: Boolean? = null) {
        if(showContentOnboarding == true || showOnboardingForItemProperties) {
            lytOnboardingText.visibility = View.VISIBLE
            lytContentWebViewAndOnboardingText.setOnClickListener { editContent() } // only enable editing content by clicking on lytContentWebViewAndOnboardingText when showing onboarding text

            val onboardingTextId = if(showContentOnboarding == true) R.string.activity_edit_item_edit_content_onboarding_text else R.string.activity_edit_item_add_item_properties_onboarding_text
            val onboardingText = if(showContentOnboarding == true) context.getText(onboardingTextId).toString() else context.getText(onboardingTextId).toString()
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
        if(isInFullscreen && fullscreenGestureListener != null) {
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
                    wbvwContent.leaveFullscreenModeAndWaitTillLeft { // leave fullscreen otherwise a lot of unwanted behaviour occurs
                        userConfirmedHelpOnUIThread()
                    }
                }
            }

            localSettings.didShowItemInformationFullscreenGesturesHelp = true
            dataManager.localSettingsUpdated()
        }
        else {
            wbvwContent.leaveFullscreenModeAndWaitTillLeft {// leave fullscreen otherwise a lot of unwanted behaviour occurs
                userConfirmedHelpOnUIThread()
            }
        }
    }


    private fun leaveFullscreenMode() {
        txtItemContentLabel.visibility = View.VISIBLE

        editItemView.itemFieldsPreview.visibility = View.VISIBLE
        editItemView.appBar.visibility = View.VISIBLE
        mayShowOnboardingTextVisibilityOnUIThread()
        editItemView.setFloatingActionButtonVisibilityOnUIThread()
    }

    private fun enterFullscreenMode() {
        txtItemContentLabel.visibility = View.GONE
        lytOnboardingText.visibility = View.GONE

        editItemView.itemFieldsPreview.visibility = View.GONE
        editItemView.appBar.visibility = View.GONE
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
            uiStatePersister.serializeStateToDiskIfNotNull(outState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, editHtmlView.getHtml()) // application crashes if objects put into bundle are too large (> 1 MB) for Android
        }

        wbvwContent.onSaveInstanceState(outState)

        return outState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(AbsSavedState.EMPTY_STATE) // don't call with state as super.onRestoreInstanceState() doesn't like a Bundle as parameter value

        (state as? Bundle)?.let { savedInstanceState ->
            this.isInEditContentMode = savedInstanceState.getBoolean(IS_IN_EDIT_CONTENT_MODE_INTENT_EXTRA_NAME, false)
            this.isInReaderMode = savedInstanceState.getBoolean(IS_IN_READER_MODE_INTENT_EXTRA_NAME, false)

            uiStatePersister.restoreStateFromDisk(savedInstanceState, CONTENT_INTENT_EXTRA_NAME, String::class.java)?.let { content ->
                contentToEdit = content
                setContentPreviewOnUIThread()
            }

            wbvwContent.restoreInstanceState(savedInstanceState)

            uiStatePersister.restoreStateFromDisk(savedInstanceState, EDIT_CONTENT_HTML_INTENT_EXTRA_NAME, String::class.java)?.let {
                editHtmlView.setHtml(it, editItemView.currentSource?.url)
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

        editHtmlView.setHtml(contentToEdit, editItemView.currentSource?.url)

        txtEnterContentHint.visibility =
                if(contentToEdit.isBlank() == false || dataManager.localSettings.didShowAddItemPropertiesHelp) View.GONE
                else View.VISIBLE

        val webViewContentLocation = IntArray(2)
        wbvwContent.getLocationInWindow(webViewContentLocation)
        val start = webViewContentLocation[1].toFloat()
        lytViewContent.visibility = View.GONE
        lytEditContent.visibility = View.VISIBLE
        playShowEditContentViewAnimation(start)
        editItemView.setFloatingActionButtonVisibilityOnUIThread()

        isInEditContentMode = true

        invalidateOptionsMenu(context as Activity)
        contentEditor.focusEditorAndShowKeyboardDelayed()
    }

    private fun playShowEditContentViewAnimation(start: Float) {
        val itemFieldsPreview = editItemView.itemFieldsPreview
        val interpolator = AccelerateInterpolator()

        val fieldsPreviewYAnimator = ObjectAnimator
                .ofFloat(itemFieldsPreview, View.Y, itemFieldsPreview.top.toFloat(), -1 * itemFieldsPreview.measuredHeight.toFloat())
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

                didContentChangeListener?.invoke(originalContent != contentToEdit) // TODO: is this really needed as actually calling didContentChangeListener in setupItemContentEditor() should be enough
                setContentPreviewOnUIThread()
            }
        }
    }




    private fun leaveEditContentView() {
        contentEditor.hideKeyboardDelayed(250) // for Samsungs we need a delay (again an exception of Samsung devices, i really dislike them)

        animator.playShowAnimation(editItemView.itemFieldsPreview)
        lytEditContent.visibility = View.GONE
        lytViewContent.visibility = View.VISIBLE
        editItemView.setFloatingActionButtonVisibilityOnUIThread()

        isInEditContentMode = false

        invalidateOptionsMenu(context as Activity)
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
            context.startActivity(intent)
        } catch(e: Exception) { log.error("Could not open url $url with other app", e) }
    }


    private fun runOnUiThread(action: () -> Unit) {
        (context as? Activity)?.runOnUiThread(action)
    }

}