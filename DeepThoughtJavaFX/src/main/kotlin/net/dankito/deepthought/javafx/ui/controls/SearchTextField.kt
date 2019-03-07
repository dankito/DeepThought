package net.dankito.deepthought.javafx.ui.controls

import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.controlsfx.control.textfield.CustomTextField
import tornadofx.*


fun EventTarget.searchtextfield(value: String? = null, op: SearchTextField.() -> Unit = {}) = opcr(this, SearchTextField().apply { if (value != null) text = value }, op)

fun EventTarget.searchtextfield(property: ObservableValue<String>, op: SearchTextField.() -> Unit = {}) = searchtextfield().apply {
    bind(property)
    op(this)
}


class SearchTextField : CustomTextField() {

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
        clearButtonPane.cursor = Cursor.DEFAULT

        clearButtonPane.setOnMouseReleased { event ->
            if(event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                clear()
            }
        }

        rightProperty().set(clearButtonPane)
    }


    private fun doCustomInitialization() {
        setOnKeyReleased { event ->
            handleKeyReleased(event)
        }
    }

    fun handleKeyReleased(event: KeyEvent) {
        if (event.code == KeyCode.ESCAPE) {
            clear()
        }
    }

}