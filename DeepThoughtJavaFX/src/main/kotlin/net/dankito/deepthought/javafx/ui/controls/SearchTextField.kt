package net.dankito.deepthought.javafx.ui.controls

import javafx.animation.FadeTransition
import javafx.beans.InvalidationListener
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.util.Duration
import org.controlsfx.control.textfield.CustomTextField
import tornadofx.*


fun EventTarget.searchtextfield(value: String? = null, op: TextField.() -> Unit = {}) = opcr(this, SearchTextField().apply { if (value != null) text = value }, op)

fun EventTarget.searchtextfield(property: ObservableValue<String>, op: SearchTextField.() -> Unit = {}) = searchtextfield().apply {
    bind(property)
    op(this)
}


class SearchTextField : CustomTextField() {

    companion object {
        private val FADE_DURATION = Duration.millis(350.0)
    }


    init {
        setupClearButtonField() // initialization of TextFields.createClearableTextField()

        doCustomInitialization()
    }


    // copied from https://bitbucket.org/controlsfx/controlsfx/src/2388702af3fa1ebf919ab0a96d5313253fd7cb79/controlsfx/src/main/java/org/controlsfx/control/textfield/TextFields.java?at=default&fileviewer=file-view-default
    private fun setupClearButtonField() {
        styleClass.add("clearable-field") //$NON-NLS-1$

        val clearButton = Region()
        clearButton.styleClass.addAll("graphic") //$NON-NLS-1$
        val clearButtonPane = StackPane(clearButton)
        clearButtonPane.styleClass.addAll("clear-button") //$NON-NLS-1$
        clearButtonPane.opacity = 0.0
        clearButtonPane.cursor = Cursor.DEFAULT
        clearButtonPane.setOnMouseReleased { clear() }
        clearButtonPane.managedProperty().bind(editableProperty())
        clearButtonPane.visibleProperty().bind(editableProperty())

        rightProperty().set(clearButtonPane)

        val fader = FadeTransition(FADE_DURATION, clearButtonPane)
        fader.cycleCount = 1

        textProperty().addListener(InvalidationListener {
            val isTextEmpty = text.isNullOrEmpty()
            val isButtonVisible = fader.node.opacity > 0

            if(isTextEmpty && isButtonVisible) {
                setButtonVisible(fader, false)
            }
            else if(!isTextEmpty && !isButtonVisible) {
                setButtonVisible(fader, true)
            }
        })
    }

    private fun setButtonVisible(fader: FadeTransition, visible: Boolean) {
        fader.fromValue = if (visible) 0.0 else 1.0
        fader.toValue = if (visible) 1.0 else 0.0
        fader.play()
    }


    private fun doCustomInitialization() {
        setOnKeyReleased { event ->
            if(event.code == KeyCode.ESCAPE) {
                clear()
            }
        }
    }

}