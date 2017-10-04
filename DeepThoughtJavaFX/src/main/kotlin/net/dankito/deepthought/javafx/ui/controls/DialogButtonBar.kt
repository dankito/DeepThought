package net.dankito.deepthought.javafx.ui.controls

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*


class DialogButtonBar(private val closeDialogListener: () -> Unit, private val saveChangesListener: (done: () -> Unit) -> Unit,
                      hasUnsavedChangesInitialValue: Boolean = false, okButtonTitle: String? = null) : View() {

    val hasUnsavedChanges = SimpleBooleanProperty(hasUnsavedChangesInitialValue)

    private val okButtonTitleValue = okButtonTitle ?: messages["action.ok"]


    override val root = anchorpane {

        hbox {
            anchorpaneConstraints {
                topAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
            }

            button(messages["action.cancel"]) {
                minHeight = 40.0
                maxHeight = 40.0
                prefWidth = 150.0
                action { closeDialog() }

                hboxConstraints {
                    marginRight = 12.0
                }
            }

            button(okButtonTitleValue) {
                minHeight = 40.0
                maxHeight = 40.0
                prefWidth = 150.0

                disableProperty().bind(hasUnsavedChanges)

                action { saveChangesAndCloseDialog() }
            }
        }
    }


    private fun closeDialog() {
        closeDialogListener.invoke()
    }

    private fun saveChangesAndCloseDialog() {
        saveChangesListener.invoke {
            closeDialog()
        }
    }

}