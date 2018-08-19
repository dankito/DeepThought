package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.input.Clipboard
import javafx.stage.Stage
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContent
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import net.dankito.utils.web.UrlUtil
import tornadofx.*
import java.util.*
import javax.inject.Inject


class JavaFXClipboardWatcher(stage: Stage, private val urlUtil: UrlUtil) {

    @Inject
    protected lateinit var optionsDetector: OptionsForClipboardContentDetector


    private var sourceOfLastClipboardContent: Any? = null

    private var clipboardContentChangedExternallyListeners: MutableSet<(JavaFXClipboardContent) -> Unit> = HashSet()

    private var clipboardContentOptionsChangedListeners: MutableSet<(OptionsForClipboardContent) -> Unit> = HashSet()


    init {
        AppComponent.component.inject(this)

        stage.focusedProperty().addListener { _, _, newValue -> checkForChangedClipboardContent(newValue) }
    }


    private fun checkForChangedClipboardContent(isStageFocused: Boolean) {
        if(isStageFocused) {
            checkForChangedClipboardContent()
        }
    }

    private fun checkForChangedClipboardContent() {
        val clipboardContent = JavaFXClipboardContent(Clipboard.getSystemClipboard(), urlUtil)
        if(clipboardContent.url != sourceOfLastClipboardContent) { // currently only urls are supported
            sourceOfLastClipboardContent = clipboardContent.url

            callClipboardContentChangedExternallyListeners(clipboardContent)

            optionsDetector.getOptionsAsync(clipboardContent) { options ->
                runLater {
                    callClipboardOptionsChangedListeners(options)
                }
            }
        }
    }


    fun addClipboardContentChangedExternallyListener(listener: (JavaFXClipboardContent) -> Unit): Boolean {
        return clipboardContentChangedExternallyListeners.add(listener)
    }

    fun removeClipboardContentChangedExternallyListener(listener: (JavaFXClipboardContent) -> Unit): Boolean {
        return clipboardContentChangedExternallyListeners.remove(listener)
    }

    private fun callClipboardContentChangedExternallyListeners(clipboardContent: JavaFXClipboardContent) {
        for(listener in clipboardContentChangedExternallyListeners) {
            listener(clipboardContent)
        }
    }


    fun addClipboardOptionsChangedListener(listener: (OptionsForClipboardContent) -> Unit): Boolean {
        return clipboardContentOptionsChangedListeners.add(listener)
    }

    fun removeClipboardOptionsChangedListener(listener: (OptionsForClipboardContent) -> Unit): Boolean {
        return clipboardContentOptionsChangedListeners.remove(listener)
    }

    private fun callClipboardOptionsChangedListeners(options: OptionsForClipboardContent) {
        for(listener in clipboardContentOptionsChangedListeners) {
            listener(options)
        }
    }

}
