package net.dankito.deepthought.javafx.ui.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import tornadofx.*


open class EditEntityField(fieldName: String, initialFieldValue: String = "") : View() {

    companion object {
        const val EntityFieldHeight = 40.0
        const val EntityFieldLabelWidth = 135.0
        const val EntityFieldTextFieldHeight = 35.0
    }


    protected val currentValue = SimpleStringProperty(initialFieldValue)

    val didValueChange = SimpleBooleanProperty()


    protected var originalValue = initialFieldValue


    protected lateinit var lblFieldName: Label

    protected lateinit var txtfldFieldValue: TextField


    init {
        currentValue.addListener { _, _, newValue -> didValueChange.value = newValue != originalValue }

        didValueChange.addListener { _, _, newValue ->
            if(newValue && lblFieldName.text.endsWith("*") == false) {
                lblFieldName.text += "*"
            }
            else if(newValue == false && lblFieldName.text.endsWith("*")) {
                lblFieldName.text = lblFieldName.text.substring(0, lblFieldName.text.length - 1)
            }
        }
    }


    override val root = hbox {

        prefHeight = EntityFieldHeight
        alignment = Pos.CENTER_LEFT

        lblFieldName = label(fieldName) {
            prefHeight = EntityFieldHeight
            prefWidth = EntityFieldLabelWidth
        }

        txtfldFieldValue = textfield {
            prefHeight = EntityFieldTextFieldHeight

            textProperty().bindBidirectional(currentValue)

            hboxConstraints {
                hGrow = Priority.ALWAYS
            }
        }

        hboxConstraints {
            marginBottom = 6.0
        }
    }


    var value: String
        get() = currentValue.value
        set(value) {
            originalValue = value
            currentValue.value = value
        }

}