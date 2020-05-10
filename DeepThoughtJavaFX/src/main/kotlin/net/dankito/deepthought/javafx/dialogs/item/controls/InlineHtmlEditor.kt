package net.dankito.deepthought.javafx.dialogs.item.controls

import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.res.icons.Icons
import net.dankito.richtexteditor.command.CommandName
import net.dankito.richtexteditor.java.fx.RichTextEditor
import net.dankito.richtexteditor.java.fx.command.SwitchTextBackgroundColorOnOffCommand
import net.dankito.richtexteditor.java.fx.toolbar.GroupedCommandsEditorToolbar
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.image.JavaFXImageReference
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

        ensureOnlyUsesSpaceIfVisible()

        addToolbar()

        javaScriptExecutor.addLoadedListener {
            setEditorFontSize(16) // TODO: make settable in settings and then save to LocalSettings
            setPadding(8.0)
            setEditorFontFamily("Georgia")
        }
    }

    private fun addToolbar() {
        val toolbar = GroupedCommandsEditorToolbar()
        this.children.add(0, toolbar.root)

        // manually add command to mark text
        toolbar.getCommand(CommandName.UNDERLINE)?.let { underlineCommand ->
            toolbar.addAfterCommand(SwitchTextBackgroundColorOnOffCommand(icon = JavaFXImageReference(Icons.MarkTextIconPath), setOnColorToCurrentColor = false), underlineCommand)
        }

        toolbar.editor = this
    }


    override fun cleanUp() {
        prefWidthProperty().unbind()

        (parent as? Pane)?.children?.remove(this)

        super.cleanUp()
    }

}