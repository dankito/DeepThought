package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.text.TextAlignment
import org.controlsfx.control.PopOver
import tornadofx.*


class CreateItemHintPopOver(private val anchorNode: Node) : PopOver() {

    init {
        this.contentNode = createContentNode()

        this.arrowLocation = PopOver.ArrowLocation.TOP_CENTER
        this.isAutoHide = false
        this.isDetachable = false

        FX.primaryStage.focusedProperty().addListener { _, _, newValue -> mainWindowIsFocusedChanged(newValue) }
    }


    private fun createContentNode(): Node {
        val node = Label(FX.messages["main.window.hint.create.item"])

        node.textAlignment = TextAlignment.CENTER
        node.isWrapText = true

        node.minHeight = 115.0
        node.prefWidth = 190.0
        node.padding = Insets(12.0)

        return node
    }


    fun showHint() {
        show(anchorNode)
    }

    private fun mainWindowIsFocusedChanged(isFocused: Boolean?) {
        if(isFocused == true) {
            showHint()
        }
        else if(isFocused == false) {
            hide()
        }
    }

}