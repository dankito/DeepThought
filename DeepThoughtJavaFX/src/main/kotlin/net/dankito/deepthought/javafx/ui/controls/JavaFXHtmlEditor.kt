package net.dankito.deepthought.javafx.ui.controls

import javafx.scene.layout.HBox
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import net.dankito.deepthought.javafx.util.FXUtils
import javafx.scene.web.WebView
import org.slf4j.LoggerFactory
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.ui.html.*
import netscape.javascript.JSObject
import java.net.CookieHandler
import java.net.CookieManager
import javax.inject.Inject


class JavaFXHtmlEditor(private val listener: IHtmlEditorListener?) : HBox(), IJavaScriptBridge, IJavaScriptExecutor {

    companion object {
        private val log = LoggerFactory.getLogger(JavaFXHtmlEditor::class.java)
    }


    @Inject
    protected lateinit var htmlEditorExtractor: HtmlEditorExtractor


    protected var webView = WebView()

    protected var engine = webView.engine

    protected var htmlEditor: HtmlEditorCommon


    init {
        AppComponent.component.inject(this)

        this.htmlEditor = HtmlEditorCommon(this, htmlEditorExtractor, listener)

        setupHtmlEditor()
    }

    private fun setupHtmlEditor() {
        minHeight = 200.0
        prefHeight = Region.USE_COMPUTED_SIZE
        maxHeight = FXUtils.SizeMaxValue
        webView.minHeight = 200.0
        webView.prefHeight = Region.USE_COMPUTED_SIZE
        webView.maxHeight = FXUtils.SizeMaxValue

        webView.isContextMenuEnabled = false
        webView.onContextMenuRequested = EventHandler<ContextMenuEvent> { event ->
            // don't why these insets are needed, figured them out by trial an error
            showContextMenuAtPosition(event.x.toInt() - 14, event.y.toInt() - 12)
        }

        this.children.add(webView)
        HBox.setHgrow(webView, Priority.ALWAYS)
        webView.prefHeightProperty().bind(this.heightProperty())
        webView.prefWidthProperty().bind(this.widthProperty())

        isFillHeight = true

        loadCKEditor()
    }

    private fun loadCKEditor() {
        engine.loadWorker.stateProperty().addListener { ov, oldState, newState ->
            if(newState === Worker.State.SUCCEEDED) {
                htmlEditor.webControlLoaded()
            }
            else if (newState === Worker.State.FAILED) {
                log.error("Loading CKEditor failed")
                // TODO: notify user
            }
        }

        htmlEditor.getHtmlEditorPath { path ->
            FXUtils.runOnUiThread { engine.load(path) }
        }
    }


    override fun ckEditorLoaded() {
    }

    override fun htmlChanged() {
    }

    override fun htmlHasBeenReset() {
    }

    override fun elementClicked(element: String, button: Int, clickX: Int, clickY: Int): Boolean {
        return true
    }

    override fun elementDoubleClicked(element: String): Boolean {
        return true
    }

    override fun beforeCommandExecution(commandName: String): Boolean {
        return true
    }

    private fun showContextMenuAtPosition(x: Int, y: Int) {
        htmlEditor.showContextMenuAtPosition(x, y)
    }

    fun getHtmlAsync(callback: (html: String) -> Unit) {
        return htmlEditor.getHtmlAsyncViaJavaScript(callback)
    }

    fun setHtml(html: String, resetUndoStack: Boolean = false) {
        htmlEditor.setHtml(html, resetUndoStack)
    }

    fun setHtmlHasBeenSaved() {
        htmlEditor.setHtmlHasBeenSaved()
    }

    fun insertHtml(html: String) {
        htmlEditor.insertHtml(html)
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
        FXUtils.runOnUiThread { executeScriptOnUiThread(javaScript, listener) }
    }

    private fun executeScriptOnUiThread(javaScript: String, listener: ((result: Any?) -> Unit)?) {
        try {
            val result = engine.executeScript(javaScript)
            listener?.invoke(result)
        } catch (e: Exception) {
            log.error("Could not execute JavaScript $javaScript", e)
            listener?.invoke(null) // TODO: what to return in this case? A NullObject? How to get JavaScript 'undefined' JSObject?
        }

    }

    override fun setJavaScriptMember(name: String, member: IJavaScriptBridge?) {
        executeScript("window") { result ->
            try {
                val win = result as JSObject
                win.setMember(name, member)
            } catch (e: Exception) {
                log.error("Could not set JavaScript member '$name' to $member", e)
            }
        }
    }


    fun cleanUp() {
        htmlEditor.cleanUp()

        // Delete cache for navigate back
        webView.engine.load("about:blank")
        try { webView.engine.history.entries.clear() } catch(ignored: Exception) { }

        // Delete cookies
        CookieHandler.setDefault(CookieManager())
    }

}