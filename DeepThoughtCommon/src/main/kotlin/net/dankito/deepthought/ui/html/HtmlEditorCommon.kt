package net.dankito.deepthought.ui.html

import org.apache.commons.lang3.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


/**
 * A Java Wrapper class for the JavaScript CKEditor.
 */
class HtmlEditorCommon(scriptExecutor: IJavaScriptExecutor, private val htmlEditorExtractor: HtmlEditorExtractor, var listener: IHtmlEditorListener? = null) : IJavaScriptBridge {

    companion object {

        val HtmlEditorFolderName = "htmleditor"

        val HtmlEditorFileName = "CKEditor_start.html"

        val HtmlEditorFolderAndFileName: String = File(HtmlEditorFolderName, HtmlEditorFileName).path


        val CKEditorInstanceName = "CKEDITOR.instances.editor"

        val JavaScriptCommandGetHtml = "CKEDITOR.instances.editor.getData()"


        private val log = LoggerFactory.getLogger(HtmlEditorCommon::class.java)
    }


    private var scriptExecutor: IJavaScriptExecutor? = scriptExecutor

    var isCKEditorLoaded = false
        private set

    private var htmlToSetWhenLoaded: String? = null

    private var editorHasBeenNewlyInitialized = false


    fun getHtmlEditorPath(callback: (path: String) -> Unit) {
        htmlEditorExtractor.addHtmlEditorExtractedListener { path ->
            callback(path)
        }
    }

    /**
     * When the Web Browser Control (WebView) has loaded, this is the earliest point of time to execute JavaScript
     */
    fun webControlLoaded() {
        scriptExecutor?.setJavaScriptMember("app", this)
    }

    fun insertHtml(html: String) {
        val escapedHtml = StringEscapeUtils.escapeEcmaScript(html)

        scriptExecutor?.executeScript(CKEditorInstanceName + ".insertHtml('" + escapedHtml + "', 'unfiltered_html')")
    }

    fun scrollTo(scrollPosition: Int) {
        scriptExecutor?.executeScript("$($CKEditorInstanceName.document.$).scrollTop($scrollPosition);")
    }

    fun resetUndoStack() {
        scriptExecutor?.executeScript(CKEditorInstanceName + ".resetUndo()")
    }

    fun releaseData() {
        scriptExecutor?.executeScript(CKEditorInstanceName + ".document.clearCustomData();")
        scriptExecutor?.executeScript(CKEditorInstanceName + ".document.removeAllListeners();")
        scriptExecutor?.executeScript(CKEditorInstanceName + ".window.getFrame().clearCustomData();")
        scriptExecutor?.executeScript(CKEditorInstanceName + ".window.getFrame().removeAllListeners();")
    }


    /**
     * For Android Version 19 and above this method has to run on a different thread than the UI thread (otherwise the call to
     * evaluateScript() would block), but for JavaFX this method must run on UI thread an no new Thread may be created
     */
    fun getHtmlAsyncViaJavaScript(callback: (html: String) -> Unit) {
        scriptExecutor?.executeScript(JavaScriptCommandGetHtml, { result ->
            if(result is String) {
                val html = getUnescapedResponse(result)

                callback(html)
            }
            else {
                callback("") // TODO: what to return in this case
            }
        })
    }

    private fun getUnescapedResponse(response: Any): String {
        var responseAsString = response as? String ?: response.toString()

        if (responseAsString.startsWith("\"") && responseAsString.endsWith("\"")) {
            responseAsString = responseAsString.substring(1, responseAsString.length - 1)

            responseAsString = StringEscapeUtils.unescapeEcmaScript(responseAsString)
        }

        return responseAsString
    }

    fun setHtml(html: String?, resetUndoStack: Boolean = false) {
        var html = html
        if (html == null) {
            html = ""
        }

        try {
            if(isCKEditorLoaded == false)
                htmlToSetWhenLoaded = html // save html so that it can be set as soon as CKEditor is loaded
            else {
                val escapedString = StringEscapeUtils.escapeEcmaScript(html)

                scriptExecutor?.executeScript("setHtml('" + escapedString + "', " + resetUndoStack + ")")
                htmlToSetWhenLoaded = null
            }
        } catch (ex: Exception) {
            log.error("Could not set HtmlEditorCommon's html text", ex)
        }

    }

    fun setHtmlHasBeenSaved() {
        scriptExecutor?.executeScript("setHtmlHasBeenSaved()")
    }

    fun showContextMenuAtPosition(x: Int, y: Int) {
        scriptExecutor?.executeScript("showContextMenu($x, $y)")
    }

    fun reInitHtmlEditor(listener: IHtmlEditorListener) {
        scrollTo(0)
        resetUndoStack()
        this.listener = listener

        editorHasBeenNewlyInitialized = true
    }


    fun cleanUp() {
        listener = null

        scriptExecutor?.setJavaScriptMember("app", null)

        this.scriptExecutor = null
    }


    /*    Methods over which JavaScript running in Browser communicates with Java code        */

    override fun ckEditorLoaded() {
        isCKEditorLoaded = true

        scriptExecutor?.executeScript("resizeEditorToFitWindow()") // don't know why but without calling it CKEditor doesn't size correctly

        if (htmlToSetWhenLoaded != null) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    setHtml(htmlToSetWhenLoaded, true)
                }
            }, 300) // i don't know why but executing Script immediately results in an error (maybe the JavaScript code is blocked till method is finished -> wait some (unrecognizable) time
        }

        listener?.editorHasLoaded(this)
    }

    override fun htmlChanged() {
        listener?.htmlCodeUpdated()

        if (editorHasBeenNewlyInitialized == true) {
            editorHasBeenNewlyInitialized = false
        }
    }

    override fun htmlHasBeenReset() {
        listener?.htmlCodeHasBeenReset()
    }

    override fun elementClicked(element: String, button: Int, clickX: Int, clickY: Int): Boolean {
        return true
    }

    override fun elementDoubleClicked(element: String): Boolean {
        return false
    }

    override fun beforeCommandExecution(commandName: String): Boolean {
        return true
    }

}
