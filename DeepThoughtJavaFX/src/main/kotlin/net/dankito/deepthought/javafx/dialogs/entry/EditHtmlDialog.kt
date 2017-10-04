package net.dankito.deepthought.javafx.dialogs.entry

import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import javafx.stage.Window
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.JavaFXHtmlEditor
import tornadofx.*


class EditHtmlDialog: DialogFragment() {

    private var htmlEditor: JavaFXHtmlEditor by singleAssign()

    private var editingDone: (html: String) -> Unit by singleAssign()

    private var closeListener: () -> Unit by singleAssign()


    override val root = vbox {
        prefWidth = 800.0
        prefHeight = 500.0

        htmlEditor = JavaFXHtmlEditor(null)
        add(htmlEditor)

        htmlEditor.vgrow = Priority.ALWAYS
        htmlEditor.useMaxWidth = true

        val buttons = DialogButtonBar({ closeDialog() }, { saveChanges(it) } )
        add(buttons)
    }


    fun show(html: String, title: String?, owner: Window?, closeListener: () -> Unit, editingDone: (html: String) -> Unit) {
        htmlEditor.setHtml(html, true)
        this.closeListener = closeListener
        this.editingDone = editingDone

        show(title, stageStyle = StageStyle.UTILITY, owner = owner)
    }

    private fun saveChanges(done: () -> Unit) {
        htmlEditor.getHtmlAsync {
            editingDone(it)

            done()
        }
    }

    private fun closeDialog() {
        runLater {
            closeListener()

            close()
        }
    }

}