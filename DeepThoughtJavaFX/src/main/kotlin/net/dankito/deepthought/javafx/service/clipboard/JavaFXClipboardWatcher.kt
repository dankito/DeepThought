package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.input.Clipboard
import javafx.stage.Stage
import net.dankito.utils.UrlUtil
import java.util.*


class JavaFXClipboardWatcher(stage: Stage, private val urlUtil: UrlUtil) {

    private var sourceOfLastShownPopup: Any? = null

    private var clipboardContentChangedExternallyListeners: MutableSet<(JavaFXClipboardContent) -> Unit> = HashSet()


    constructor(stage: Stage, urlUtil: UrlUtil, listener: (JavaFXClipboardContent) -> Unit) : this(stage, urlUtil) {
        addClipboardContentChangedExternallyListener(listener)
    }


    init {
        stage.focusedProperty().addListener { _, _, newValue -> checkForChangedClipboardContent(newValue) }
    }


    private fun checkForChangedClipboardContent(isStageFocused: Boolean) {
        if(isStageFocused) {
            checkForChangedClipboardContent()
        }
    }

    private fun checkForChangedClipboardContent() {
        val clipboardContent = JavaFXClipboardContent(Clipboard.getSystemClipboard(), urlUtil)
//        Application.getContentExtractorManager().getContentExtractorOptionsForClipboardContentAsync(clipboardContent, { contentExtractOptions ->
//            if (contentExtractOptions.getSource().equals(sourceOfLastShownPopup) === false) {
//                sourceOfLastShownPopup = contentExtractOptions.getSource()
                callClipboardContentChangedExternallyListeners(clipboardContent)
//            }
//        })
    }

    private fun callClipboardContentChangedExternallyListeners(clipboardContent: JavaFXClipboardContent) {
        for(listener in clipboardContentChangedExternallyListeners) {
            listener(clipboardContent)
        }
    }


    fun addClipboardContentChangedExternallyListener(listener: (JavaFXClipboardContent) -> Unit): Boolean {
        return clipboardContentChangedExternallyListeners.add(listener)
    }

    fun removeClipboardContentChangedExternallyListener(listener: (JavaFXClipboardContent) -> Unit): Boolean {
        return clipboardContentChangedExternallyListeners.remove(listener)
    }

}
