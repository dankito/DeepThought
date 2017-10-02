package net.dankito.deepthought.android.views.html

import android.annotation.TargetApi
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.AttributeSet
import android.webkit.*
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.ui.html.*
import net.dankito.utils.OsHelper
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AndroidHtmlEditor : WebView, IJavaScriptBridge, IJavaScriptExecutor {

    companion object {
        private val log = LoggerFactory.getLogger(AndroidHtmlEditor::class.java)
    }


    @Inject
    protected lateinit var htmlEditorExtractor: HtmlEditorExtractor

    @Inject
    protected lateinit var osHelper: OsHelper

    @Inject
    protected lateinit var activityTracker: CurrentActivityTracker


    private val htmlEditor: HtmlEditorCommon

    private var javaScriptBridgesToCall: MutableMap<String, IJavaScriptBridge> = ConcurrentHashMap()


    init {
        AppComponent.component.inject(this)

        htmlEditor = HtmlEditorCommon(this, htmlEditorExtractor)
    }

    constructor(context: Context) : super(context) {
        setupHtmlEditor(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupHtmlEditor(null)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupHtmlEditor(null)
    }

    constructor(context: Context, listener: IHtmlEditorListener) : super(context) {
        setupHtmlEditor(listener)
    }


    private fun setupHtmlEditor(listener: IHtmlEditorListener?) {
        this.settings.javaScriptEnabled = true

        if(osHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(14))
            settings.textZoom = 85
        else
            setInitialScale(95)

        htmlEditor.listener = listener

        setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                htmlEditor.webControlLoaded()
                executeScript("resizeEditorToFitWindow()")
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                log.error("An error occurred in WebView when calling Url $failingUrl: $description")
                super.onReceivedError(view, errorCode, description, failingUrl)
            }
        })

        setWebChromeClient(object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                //        return super.onJsAlert(view, url, message, result);
                result.confirm() // do not show any JavaScript alert to user
                return true
            }
        })

        addJavascriptInterface(this, "app") // has to be set already here otherwise loaded event will not be recognized
        if(osHelper.isRunningOnAndroidAtLeastOfApiLevel(19) == false) { // before Android 19 there was no way to get automatically informed of JavaScript results -> use this as workaround
            addJavascriptInterface(this, "android")
        }

        htmlEditor.getHtmlEditorPath { path ->
            activityTracker.currentActivity?.runOnUiThread { loadUrl(path) }
        }

        requestFocus()
    }


    fun insertHtml(html: String) {
        htmlEditor.insertHtml(html)
    }


    /**
     * For Android 19 and above the Javascript bridge may not be called on the UI thread -> this is why this method spawns a new thread and to get html from HtmlEditor
     */
    fun getHtmlAsync(callback: (html: String) -> Unit) {
        htmlEditor.getHtmlAsyncViaJavaScript(callback)
    }

    fun setHtml(html: String, resetUndoStack: Boolean = false) {
        htmlEditor.setHtml(html, resetUndoStack)
    }

    fun setHtmlHasBeenSaved() {
        htmlEditor.setHtmlHasBeenSaved()
    }

    fun showContextMenuAtPosition(x: Int, y: Int) {
        htmlEditor.showContextMenuAtPosition(x, y)
    }


    fun reInitHtmlEditor(listener: IHtmlEditorListener) {
        htmlEditor.reInitHtmlEditor(listener)
    }

    fun resetInstanceVariables() {
        htmlEditor.listener = null
        htmlEditor.setHtml("", true)
        htmlEditor.releaseData()
    }


    override fun executeScript(javaScript: String, listener: ((result: Any?) -> Unit)?) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            executeScriptOnUiThread(javaScript, listener)
        }
        else if(activityTracker.currentActivity != null) {
            activityTracker.currentActivity?.runOnUiThread { executeScriptOnUiThread(javaScript, listener) }
        }
        else {
            log.error("Trying to execute Script '$javaScript', but activity is null")
        }
    }

    private fun executeScriptOnUiThread(javaScript: String, listener: ((result: Any?) -> Unit)?) {
        try {
            if(osHelper.isRunningOnAndroidAtLeastOfApiLevel(19)) {
                executeScriptOnUiThreadForAndroid19AndAbove(javaScript, listener)
            }
            else {
                executeScriptOnUiThreadForAndroidPre19(javaScript, listener)
            }

        } catch (ex: Exception) {
            log.error("Could not evaluate JavaScript " + javaScript, ex)
        }

    }

    @TargetApi(19)
    private fun executeScriptOnUiThreadForAndroid19AndAbove(javaScript: String, listener: ((result: Any?) -> Unit)?) {
        // evaluateJavascript() only works on API 19 and newer!
        evaluateJavascript(javaScript) { value ->
            listener?.invoke(value)
        }
    }

    private fun executeScriptOnUiThreadForAndroidPre19(javaScript: String, listener: ((result: Any?) -> Unit)?) {
        if (listener == null) { // no response is needed
            loadUrl("javascript:" + javaScript)
        }
        else {
            // as via loadUrl() we cannot execute JavaScript and wait for its result ->
            // for each JavaScript Method create an extra responseToXyz method (like responseToGetHtml() below) in ckeditor_control.js, with it then call to tell us result
            if (javaScript === HtmlEditorCommon.JavaScriptCommandGetHtml) {
                listenerForGetHtml = listener
                waitForGetHtmlResponseLatch = CountDownLatch(1)

                loadUrl("javascript:androidGetHtml()")

                // i really hate writing this code as method runs on UI thread and in worst case UI thread gets then blocked
                try {
                    waitForGetHtmlResponseLatch?.await(500, TimeUnit.MILLISECONDS)
                } catch (ex: Exception) {
                }

                listenerForGetHtml = null
            } else {
                log.error("An unknown JavaScript command with result has been executed, add handling for it in AndroidHtmlEditor")
            }
        }
    }


    /*  Response handling as Android pre 19 doesn't support getting result of executed JavaScript    */

    private var listenerForGetHtml: ((result: Any?) -> Unit)? = null
    private var waitForGetHtmlResponseLatch: CountDownLatch? = null

    @JavascriptInterface
    fun responseToGetHtml(htmlData: String) {
        listenerForGetHtml?.invoke(htmlData)
    }


    override fun setJavaScriptMember(name: String, member: IJavaScriptBridge?) {
        // since Android Api 17 all methods callable from JavaScript must be annotated with @JavascriptInterface, an Android specific annotation
        // -> HtmlEditor cannot know this annotation, so we save the member instance, let the method call on ourselves and then pass the method call on to the member
        if(member != null) {
            javaScriptBridgesToCall.put(name, member)
        }
        else {
            javaScriptBridgesToCall.remove(name)
        }
    }

    @JavascriptInterface
    override fun ckEditorLoaded() {
        for(bridge in javaScriptBridgesToCall.values) {
            bridge.ckEditorLoaded()
        }
    }

    @JavascriptInterface
    override fun htmlChanged() {
        for(bridge in javaScriptBridgesToCall.values) {
            bridge.htmlChanged()
        }
    }

    @JavascriptInterface
    override fun htmlHasBeenReset() {
        for(bridge in javaScriptBridgesToCall.values) {
            bridge.htmlHasBeenReset()
        }
    }

    @JavascriptInterface
    override fun elementClicked(element: String, button: Int, clickX: Int, clickY: Int): Boolean {
        var result = true

        for(bridge in javaScriptBridgesToCall.values) {
            result = result and bridge.elementClicked(element, button, clickX, clickY)
        }

        return result
    }

    @JavascriptInterface
    override fun elementDoubleClicked(element: String): Boolean {
        var result = true

        for(bridge in javaScriptBridgesToCall.values) {
            result = result and bridge.elementDoubleClicked(element)
        }

        return result
    }

    @JavascriptInterface
    override fun beforeCommandExecution(commandName: String): Boolean {
        var result = true

        for(bridge in javaScriptBridgesToCall.values) {
            result = result and bridge.beforeCommandExecution(commandName)
        }

        if(commandName == "paste") {
            result = pasteDataFromClipboard()
        }

        return result
    }

    private fun pasteDataFromClipboard(): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if(clipboard.hasPrimaryClip()) { // clipboard contains data
            var text: String = ""

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                text = clipboard.primaryClip.getItemAt(0).htmlText
            }
            else if(clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                text = clipboard.primaryClip.getItemAt(0).text.toString()
            }

            if(text.isNullOrBlank() == false) {
                insertHtml(text)
            }
        }

        return false // default implementation does nothing
    }

    val isLoaded: Boolean
        get() = htmlEditor.isCKEditorLoaded ?: false


    fun cleanUp() {
        htmlEditor.cleanUp()
    }

}
