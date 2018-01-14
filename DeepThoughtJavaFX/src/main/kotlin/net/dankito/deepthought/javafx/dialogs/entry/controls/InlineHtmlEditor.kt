package net.dankito.deepthought.javafx.dialogs.entry.controls

import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.richtexteditor.java.fx.RichTextEditor
import tornadofx.*


class InlineHtmlEditor : RichTextEditor() {

    val didHtmlChange: Boolean
        get() = javaScriptExecutor.didHtmlChange


    init {
        setupUi()
    }


    private fun setupUi() {
        vgrow = Priority.ALWAYS
        useMaxWidth = true

        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

        javaScriptExecutor.addLoadedListener {
            setEditorFontSize(16) // TODO: make settable in settings and then save to LocalSettings
            setPadding(8.0)
            setEditorFontFamily("serif")
        }
    }


    fun cleanUp() {
        prefWidthProperty().unbind()

        (parent as Pane).children.remove(this)
    }

}