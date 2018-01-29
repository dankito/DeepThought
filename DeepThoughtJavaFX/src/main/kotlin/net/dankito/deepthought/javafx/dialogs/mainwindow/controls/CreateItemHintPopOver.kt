package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.text.TextAlignment
import org.controlsfx.control.PopOver
import tornadofx.*


class CreateItemHintPopOver(private val anchorNode: Node, private val hintHiddenListener: () -> Unit) : PopOver() {

    private val mainWindowIsFocusedListener = ChangeListener { _, _, newValue: Boolean ->
        mainWindowIsFocusedChanged(newValue)
    }


    init {
        this.contentNode = createContentNode()
        contentNode.setOnMouseClicked { e -> if(e.button == MouseButton.PRIMARY) hideHint() }

        this.arrowLocation = PopOver.ArrowLocation.TOP_CENTER
        this.isAutoHide = false
        this.isDetachable = false

        FX.primaryStage.focusedProperty().addListener(mainWindowIsFocusedListener)
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

    fun hideHint() {
        hide()

        FX.primaryStage.focusedProperty().removeListener(mainWindowIsFocusedListener)

        hintHiddenListener()
    }

    private fun mainWindowIsFocusedChanged(isFocused: Boolean?) {
        if(isFocused == true) {
            showHint()
        }
        else if(isFocused == false) {
            hide() // don't call hideHint() as we don't want to clean PopOver but just hide it till MainWindow gets re-focused
        }
    }

}